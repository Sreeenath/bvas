// src/main/resources/static/js/api.js

const API_BASE_URL = 'http://localhost:8080/api';

// Token management
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');

// API Helper Functions
async function apiCall(endpoint, method = 'GET', body = null, isFormData = false) {
    const headers = {
        'Authorization': `Bearer ${authToken}`
    };
    
    if (!isFormData && body) {
        headers['Content-Type'] = 'application/json';
    }
    
    const options = {
        method,
        headers
    };
    
    if (body) {
        if (isFormData) {
            options.body = body;
        } else {
            options.body = JSON.stringify(body);
        }
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || data.message || 'Request failed');
        }
        
        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Auth API
const authAPI = {
    async login(username, password) {
        const response = await apiCall('/auth/login', 'POST', { username, password });
        if (response.success && response.data) {
            authToken = response.data.token;
            currentUser = {
                username: response.data.username,
                role: response.data.role,
                fullName: response.data.fullName,
                userId: response.data.userId
            };
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            return response.data;
        }
        throw new Error(response.error || 'Login failed');
    },
    async forgotPassword(usernameOrMobile) {
        const response = await apiCall('/auth/forgot-password', 'POST', {
            usernameOrMobile: usernameOrMobile
        });
        return response;
    },
    
    async resetPassword(usernameOrMobile, otp, newPassword, confirmPassword) {
        const response = await apiCall('/auth/reset-password', 'POST', {
            usernameOrMobile: usernameOrMobile,
            otp: otp,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        });
        return response;
    },
    
    async register(userData) {
        return await apiCall('/auth/register', 'POST', userData);
    },
    
    logout() {
        authToken = null;
        currentUser = {};
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        window.location.href = '/login.html';
    }
};

// Vendor API
const vendorAPI = {
    async getBills() {
        const response = await apiCall('/vendor/bills');
        return response.data || [];
    },
    
    async getBill(id) {
        const response = await apiCall(`/vendor/bills/${id}`);
        return response.data;
    },
    
    async submitBill(billData, files) {
        const formData = new FormData();
        formData.append('billMonth', billData.billMonth);
        formData.append('billYear', billData.billYear);
        formData.append('vendorRemarks', billData.vendorRemarks || '');
        billData.billItems.forEach((it, i) => {
            formData.append(`billItems[${i}].districtId`, it.districtId);
            formData.append(`billItems[${i}].quantity`, it.quantity);
            formData.append(`billItems[${i}].amount`, it.amount ?? 0);
          });
        
        if (files && files.length > 0) {
            files.forEach(f => formData.append("files", f));
        }
        
        return await apiCall('/vendor/bills', 'POST', formData, true);
    },
    
    async resubmitBill(billId, billData, files) {
        const formData = new FormData();
        formData.append('billMonth', billData.billMonth);
        formData.append('billYear', billData.billYear);
        formData.append('vendorRemarks', billData.vendorRemarks || '');
        billData.billItems.forEach((it, i) => {
            formData.append(`billItems[${i}].districtId`, it.districtId);
            formData.append(`billItems[${i}].quantity`, it.quantity);
            formData.append(`billItems[${i}].amount`, it.amount ?? 0);
          });        
        if (files && files.length > 0) {
            files.forEach(f => formData.append("files", f));
        }
        
        return await apiCall(`/vendor/bills/${billId}/resubmit`, 'POST', formData, true);
    },
    
    async downloadDocument(billId, documentId) {
        const response = await fetch(`${API_BASE_URL}/vendor/bills/${billId}/documents/${documentId}/download`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `document-${documentId}`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        }
    }
};

// District API
const districtAPI = {
    async getPendingBills() {
        const response = await apiCall('/district/bills/pending');
        return response.data || [];
    },
    
    async getBill(id) {
        const response = await apiCall(`/district/bills/${id}`);
        return response.data;
    },
    
    async approveBill(billId, remarks) {
        return await apiCall('/district/bills/approve', 'POST', {
            billId,
            action: 'APPROVE',
            remarks: remarks || '',
            reAuthPassword: prompt('Enter your password for re-authentication:') || ''
        });
    },
    
    async rejectBill(billId, remarks) {
        return await apiCall('/district/bills/approve', 'POST', {
            billId,
            action: 'REJECT',
            remarks: remarks,
            reAuthPassword: prompt('Enter your password for re-authentication:') || ''
        });
    },
    
    async signBill(signatureData) {
        return await apiCall('/district/bills/sign', 'POST', signatureData);
    }
};

// Admin API
const adminAPI = {
    async getDashboardKPIs() {
        const response = await apiCall('/admin/dashboard/kpis');
        return response.data;
    },
    
    async getUsers(role = null, isActive = null, isApproved = null) {
        let endpoint = '/admin/users';
        const params = new URLSearchParams();
        if (role) params.append('role', role);
        if (isActive !== null) params.append('isActive', isActive);
        if (isApproved !== null) params.append('isApproved', isApproved);
        if (params.toString()) endpoint += '?' + params.toString();
        
        const response = await apiCall(endpoint);
        return response.data || [];
    },
    
    async createUser(userData) {
        return await apiCall('/admin/users', 'POST', userData);
    },
    
    async updateUser(userId, userData) {
        return await apiCall(`/admin/users/${userId}`, 'PUT', userData);
    },
    
    async approveUser(userId) {
        return await apiCall(`/admin/users/${userId}/approve`, 'PUT');
    },
    
    async blockUser(userId, remarks) {
        return await apiCall(`/admin/users/${userId}/block?remarks=${encodeURIComponent(remarks || '')}`, 'PUT');
    },
    
    async unblockUser(userId) {
        return await apiCall(`/admin/users/${userId}/unblock`, 'PUT');
    },
    
    async manageSubmissionWindow(windowData) {
        return await apiCall('/admin/submission-windows', 'POST', windowData);
    },
    
    async getAuditLogs(entityType = null, entityId = null, action = null, userId = null) {
        let endpoint = '/admin/audit-logs';
        const params = new URLSearchParams();
        if (entityType) params.append('entityType', entityType);
        if (entityId) params.append('entityId', entityId);
        if (action) params.append('action', action);
        if (userId) params.append('userId', userId);
        if (params.toString()) endpoint += '?' + params.toString();
        
        const response = await apiCall(endpoint);
        return response.data || [];
    }
};

// OTP API
const otpAPI = {
    async generateOtp(identifier) {
        const response = await apiCall(`/public/otp/generate?identifier=${identifier}`, 'POST');
        return response.data;
    },
    
    async generateAadhaarOtp(aadhaarNumber) {
        const response = await apiCall(`/public/otp/aadhaar/generate?aadhaarNumber=${aadhaarNumber}`, 'POST');
        return response.data;
    }
};

// Utility Functions
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    document.body.insertBefore(alertDiv, document.body.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getStatusBadge(status) {
    const statusMap = {
        'PENDING_DISTRICT_VERIFICATION': { class: 'badge-pending', text: 'Pending' },
        'APPROVED': { class: 'badge-approved', text: 'Approved' },
        'REJECTED': { class: 'badge-rejected', text: 'Rejected' },
        'DRAFT': { class: 'badge-draft', text: 'Draft' }
    };
    
    const statusInfo = statusMap[status] || { class: 'badge-secondary', text: status };
    return `<span class="badge ${statusInfo.class}">${statusInfo.text}</span>`;
}

// Check authentication
function checkAuth() {
    if (!authToken) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    if (currentUser.username) {
        const userInfoEl = document.getElementById('userInfo');
        if (userInfoEl) {
            userInfoEl.textContent = `${currentUser.fullName} (${currentUser.role})`;
        }
    }
});