let districts = [];

document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    if (currentUser.role !== 'HQ_ADMIN') {
        window.location.href = '/login.html';
        return;
    }
    
    loadDistricts();
    loadDashboardKPIs();
    loadUsers();
    
    // Show/hide district selection based on role
    document.getElementById('userRole').addEventListener('change', function() {
        const districtGroup = document.getElementById('districtSelectionGroup');
        if (this.value === 'DISTRICT_VERIFIER') {
            districtGroup.style.display = 'block';
            // Validate that at least one district is selected
            const form = document.getElementById('createUserForm');
            form.addEventListener('submit', validateDistrictSelection);
        } else {
            districtGroup.style.display = 'none';
        }
    });
});

async function loadDistricts() {
    try {
        const response = await apiCall('/admin/districts');
        districts = response.data || [];
        renderDistrictCheckboxes();
    } catch (error) {
        console.error('Failed to load districts:', error);
        // Fallback to hardcoded list if API fails
        districts = [
            { id: 1, name: 'Almora', code: 'ALM' },
            { id: 2, name: 'Bageshwar', code: 'BAG' },
            { id: 3, name: 'Chamoli', code: 'CHA' },
            { id: 4, name: 'Champawat', code: 'CHP' },
            { id: 5, name: 'Dehradun', code: 'DEH' },
            { id: 6, name: 'Haridwar', code: 'HAR' },
            { id: 7, name: 'Nainital', code: 'NAI' },
            { id: 8, name: 'Pauri Garhwal', code: 'PAU' },
            { id: 9, name: 'Pithoragarh', code: 'PIT' },
            { id: 10, name: 'Rudraprayag', code: 'RUD' },
            { id: 11, name: 'Tehri Garhwal', code: 'TEH' },
            { id: 12, name: 'Udham Singh Nagar', code: 'UDH' },
            { id: 13, name: 'Uttarkashi', code: 'UTT' }
        ];
        renderDistrictCheckboxes();
    }
}

function renderDistrictCheckboxes() {
    const container = document.getElementById('districtCheckboxes');
    if (!container) return;
    
    if (districts.length === 0) {
        container.innerHTML = '<p class="text-secondary">No districts available</p>';
        return;
    }
    
    container.innerHTML = districts.map(district => `
        <div style="margin-bottom: 0.75rem; padding: 0.5rem; border-radius: 4px; transition: background-color 0.2s;">
            <label style="display: flex; align-items: center; cursor: pointer; user-select: none;">
                <input type="checkbox" 
                       value="${district.id}" 
                       class="district-checkbox" 
                       style="margin-right: 0.75rem; width: 18px; height: 18px; cursor: pointer;">
                <div>
                    <strong>${district.name}</strong>
                    <span style="color: var(--text-secondary); margin-left: 0.5rem;">(${district.code})</span>
                </div>
            </label>
        </div>
    `).join('');
    
    // Add hover effect
    container.querySelectorAll('div').forEach(div => {
        div.addEventListener('mouseenter', function() {
            this.style.backgroundColor = 'rgba(37, 99, 235, 0.1)';
        });
        div.addEventListener('mouseleave', function() {
            this.style.backgroundColor = 'transparent';
        });
    });
}

function validateDistrictSelection(e) {
    const role = document.getElementById('userRole').value;
    if (role === 'DISTRICT_VERIFIER') {
        const selectedDistricts = Array.from(document.querySelectorAll('.district-checkbox:checked'));
        if (selectedDistricts.length === 0) {
            e.preventDefault();
            showAlert('Please select at least one district for District Verifier', 'error');
            return false;
        }
    }
    return true;
}

function showTab(tab) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.add('d-none'));
    document.getElementById(tab + 'Tab').classList.remove('d-none');
}

async function loadDashboardKPIs() {
    try {
        const kpis = await adminAPI.getDashboardKPIs();
        displayKPIs(kpis);
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

function displayKPIs(kpis) {
    const container = document.getElementById('kpiContainer');
    container.innerHTML = `
        <div class="kpi-card">
            <div class="kpi-value">${kpis.totalPendingBills || 0}</div>
            <div class="kpi-label">Total Pending Bills</div>
        </div>
        <div class="kpi-card success">
            <div class="kpi-value">${kpis.totalApprovedBills || 0}</div>
            <div class="kpi-label">Total Approved Bills</div>
        </div>
        <div class="kpi-card danger">
            <div class="kpi-value">${kpis.totalRejectedBills || 0}</div>
            <div class="kpi-label">Total Rejected Bills</div>
        </div>
        <div class="kpi-card warning">
            <div class="kpi-value">${kpis.currentMonthPending || 0}</div>
            <div class="kpi-label">Current Month Pending</div>
        </div>
    `;
}

async function loadUsers() {
    const container = document.getElementById('usersTableContainer');
    container.innerHTML = '<div class="spinner"></div>';
    
    try {
        const users = await adminAPI.getUsers();
        displayUsers(users);
    } catch (error) {
        container.innerHTML = `<div class="alert alert-error">${error.message}</div>`;
    }
}

function displayUsers(users) {
    const container = document.getElementById('usersTableContainer');
    
    if (users.length === 0) {
        container.innerHTML = '<p class="text-center">No users found</p>';
        return;
    }
    
    const table = `
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Full Name</th>
                    <th>Role</th>
                    <th>Districts</th>
                    <th>Status</th>
                    <th>Approved</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${users.map(user => `
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.username}</td>
                        <td>${user.fullName}</td>
                        <td>${user.role}</td>
                        <td>${user.assignedDistricts && user.assignedDistricts.length > 0 
                            ? user.assignedDistricts.map(d => d.name).join(', ') 
                            : '-'}</td>
                        <td>${user.isActive ? '<span class="badge badge-approved">Active</span>' : '<span class="badge badge-rejected">Blocked</span>'}</td>
                        <td>${user.isApproved ? '<span class="badge badge-approved">Yes</span>' : '<span class="badge badge-pending">No</span>'}</td>
                        <td>
                            ${!user.isApproved ? `<button class="btn btn-sm btn-success" onclick="approveUser(${user.id})">Approve</button>` : ''}
                            ${!user.isApproved ? `<button class="btn btn-sm btn-danger" onclick="rejectUser(${user.id})">Reject</button>` : ''}
                            ${user.isActive ? `<button class="btn btn-sm btn-danger" onclick="blockUser(${user.id})">Block</button>` : `<button class="btn btn-sm btn-success" onclick="unblockUser(${user.id})">Unblock</button>`}
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    
    container.innerHTML = table;
}

function showCreateUserModal() {
    document.getElementById('createUserForm').reset();
    document.getElementById('districtSelectionGroup').style.display = 'none';
    // Uncheck all districts
    document.querySelectorAll('.district-checkbox').forEach(cb => cb.checked = false);
    document.getElementById('createUserModal').style.display = 'block';
}

document.getElementById('createUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Validate district selection for DISTRICT_VERIFIER
    const role = document.getElementById('userRole').value;
    if (role === 'DISTRICT_VERIFIER') {
        const selectedDistricts = Array.from(document.querySelectorAll('.district-checkbox:checked'));
        if (selectedDistricts.length === 0) {
            showAlert('Please select at least one district for District Verifier', 'error');
            return;
        }
    }
    
    try {
        const selectedDistricts = Array.from(document.querySelectorAll('.district-checkbox:checked'))
            .map(cb => parseInt(cb.value));
        
        const userData = {
            username: document.getElementById('userUsername').value,
            password: document.getElementById('userPassword').value,
            fullName: document.getElementById('userFullName').value,
            email: document.getElementById('userEmail').value,
            mobileNumber: document.getElementById('userMobile').value,
            role: document.getElementById('userRole').value,
            isActive: true
        };
        
        // Add district IDs if role is DISTRICT_VERIFIER
        if (userData.role === 'DISTRICT_VERIFIER' && selectedDistricts.length > 0) {
            userData.districtIds = selectedDistricts;
        }
        
        await adminAPI.createUser(userData);
        showAlert('User created successfully!', 'success');
        closeModal('createUserModal');
        loadUsers();
    } catch (error) {
        showAlert(error.message, 'error');
    }
});

async function approveUser(userId) {
    try {
        await adminAPI.approveUser(userId);
        showAlert('User approved successfully!', 'success');
        loadUsers();
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

async function blockUser(userId) {
    const remarks = prompt('Enter reason for blocking:') || '';
    if (confirm('Are you sure you want to block this user?')) {
        try {
            await adminAPI.blockUser(userId, remarks);
            showAlert('User blocked successfully!', 'success');
            loadUsers();
        } catch (error) {
            showAlert(error.message, 'error');
        }
    }
}

async function unblockUser(userId) {
    try {
        await adminAPI.unblockUser(userId);
        showAlert('User unblocked successfully!', 'success');
        loadUsers();
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

async function rejectUser(userId) {
    const reason = prompt('Enter reason for rejection:') || 'Your registration could not be approved at this time.';
    if (confirm('Are you sure you want to reject this user? An email will be sent.')) {
        try {
            await apiCall(`/admin/users/${userId}/reject?reason=${encodeURIComponent(reason)}`, 'PUT');
            showAlert('Rejection email sent successfully!', 'success');
            loadUsers();
        } catch (error) {
            showAlert(error.message, 'error');
        }
    }
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

window.onclick = function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
}