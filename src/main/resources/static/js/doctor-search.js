document.addEventListener('DOMContentLoaded', function () {
    const input = document.getElementById('searchInput');
    const btn = document.getElementById('searchBtn');
    const tableBody = document.querySelector('#doctorTable tbody');

    function searchDoctors() {
        const keyword = input.value;
        fetch('/api/doctors/search?keyword=' + encodeURIComponent(keyword))
            .then(response => response.json())
            .then(doctors => {
                tableBody.innerHTML = '';
                doctors.forEach(doctor => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${doctor.name}</td>
                        <td>${doctor.specialty}</td>
                        <td>${doctor.department ? doctor.department.name : ''}</td>
                        <td><img src="${doctor.image || ''}" alt="avatar" width="80" height="80"></td>
                        <td>
                            <form action="/appointments/book/${doctor.id}" method="post">
                                <input type="date" name="date" required />
                                <button type="submit">Đặt lịch</button>
                            </form>
                        </td>
                    `;
                    tableBody.appendChild(row);
                });
            });
    }

    btn.addEventListener('click', function () {
        searchDoctors();
    });

    input.addEventListener('keyup', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            searchDoctors();
        }
    });
});