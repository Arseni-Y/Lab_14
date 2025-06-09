document.addEventListener('DOMContentLoaded', () => {
    const userSelect = document.getElementById('userSelect');
    const qrcodeSection = document.getElementById('qrcodeSection');
    const qrcodeList = document.getElementById('qrcodeList');
    const addQRCodeForm = document.getElementById('addQRCodeForm');
    const editQRCodeSection = document.getElementById('editQRCodeSection');
    const editQRCodeForm = document.getElementById('editQRCodeForm');

    userSelect.addEventListener('change', () => {
        const selectedUserIds = Array.from(userSelect.selectedOptions).map(option => option.value);
        if (selectedUserIds.length > 0) {
            fetchQRCodes(selectedUserIds);
            qrcodeSection.style.display = 'block';
            document.getElementById('selectedUserIds').value = selectedUserIds.join(',');
        } else {
            qrcodeSection.style.display = 'none';
            qrcodeList.innerHTML = '';
        }
    });

    function fetchQRCodes(userIds) {
        Promise.all(userIds.map(userId =>
            fetch(`/api/qrcodes/user/${userId}`).then(response => response.json())
        )).then(results => {
            const allQRCodes = [].concat(...results);
            renderQRCodes(allQRCodes);
        }).catch(error => console.error('Error fetching QR codes:', error));
    }

    function renderQRCodes(qrcodes) {
        qrcodeList.innerHTML = '';
        qrcodes.forEach(qr => {
            const li = document.createElement('li');
            li.className = 'mb-2';
            li.innerHTML = `
                <span>${qr.data}</span>
                <form action="/api/qrcodes/${qr.id}" method="delete" style="display:inline;">
                    <button type="submit" class="btn btn-danger ml-2">Delete</button>
                </form>
                <a href="/api/qrcodes/edit?id=${qr.id}" class="btn btn-primary ml-2">Edit</a>
            `;
            qrcodeList.appendChild(li);
        });
    }

    addQRCodeForm.addEventListener('submit', (e) => {
        const userIds = Array.from(userSelect.selectedOptions).map(option => option.value).join(',');
        document.getElementById('selectedUserIds').value = userIds;
    });

    function cancelEdit() {
        editQRCodeSection.style.display = 'none';
        window.location.href = '/';
    }

    if (window.location.search.includes('id=')) {
        const urlParams = new URLSearchParams(window.location.search);
        const qrId = urlParams.get('id');
        fetch(`/api/qrcodes/${qrId}`)
            .then(response => response.json())
            .then(qr => {
                editQRCodeSection.style.display = 'block';
                document.getElementById('editData').value = qr.data;
                document.getElementById('editUserIds').value = Array.from(userSelect.selectedOptions)
                    .map(option => option.value)
                    .join(',');
            })
            .catch(error => console.error('Error fetching QR code for edit:', error));
    }
});