// src/main/resources/static/js/district.js

let currentBill = null;

document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    if (currentUser.role !== 'DISTRICT_VERIFIER') {
        window.location.href = '/login.html';
        return;
    }
    
    loadPendingBills();
});

async function loadPendingBills() {
    const container = document.getElementById('billsTableContainer');
    container.innerHTML = '<div class="spinner"></div>';
    
    try {
        const bills = await districtAPI.getPendingBills();
        displayBills(bills);
    } catch (error) {
        container.innerHTML = `<div class="alert alert-error">${error.message}</div>`;
    }
}

function displayBills(bills) {
    const container = document.getElementById('billsTableContainer');
    
    if (bills.length === 0) {
        container.innerHTML = '<p class="text-center">No pending bills</p>';
        return;
    }
    
    const table = `
        <table class="table">
            <thead>
                <tr>
                    <th>Bill ID</th>
                    <th>Vendor</th>
                    <th>Month/Year</th>
                    <th>Total Amount</th>
                    <th>Submitted At</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${bills.map(bill => `
                    <tr>
                        <td>${bill.id}</td>
                        <td>${bill.vendor?.fullName || 'N/A'}</td>
                        <td>${bill.billMonth}/${bill.billYear}</td>
                        <td>₹${bill.totalAmount || '0.00'}</td>
                        <td>${formatDate(bill.submittedAt)}</td>
                        <td>
                            <button class="btn btn-sm btn-primary" onclick="viewBillDetails(${bill.id})">View</button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    
    container.innerHTML = table;
}

async function viewBillDetails(billId) {
    try {
        currentBill = await districtAPI.getBill(billId);
        showBillDetails(currentBill);
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

function showBillDetails(bill) {
    const content = document.getElementById('billDetailsContent');
    content.innerHTML = `
        <div class="mb-3">
            <strong>Bill ID:</strong> ${bill.id}<br>
            <strong>Vendor:</strong> ${bill.vendor?.fullName || 'N/A'}<br>
            <strong>Month/Year:</strong> ${bill.billMonth}/${bill.billYear}<br>
            <strong>Total Amount:</strong> ₹${bill.totalAmount || '0.00'}<br>
            <strong>Status:</strong> ${getStatusBadge(bill.status)}<br>
            <strong>Remarks:</strong> ${bill.vendorRemarks || 'None'}
        </div>
        
        <h3>District-wise Quantities</h3>
        <table class="table">
            <thead>
                <tr>
                    <th>District</th>
                    <th>Quantity</th>
                    <th>ePOS Quantity</th>
                    <th>Discrepancy</th>
                    <th>Amount</th>
                </tr>
            </thead>
            <tbody>
                ${bill.billItems?.map(item => `
                    <tr ${item.hasDiscrepancy ? 'style="background-color: #fee2e2;"' : ''}>
                        <td>${item.district?.name || 'N/A'}</td>
                        <td>${item.quantity}</td>
                        <td>${item.eposQuantity || '-'}</td>
                        <td>${item.hasDiscrepancy ? '⚠️ Yes' : '✓ No'}</td>
                        <td>₹${item.amount || '0.00'}</td>
                    </tr>
                `).join('') || '<tr><td colspan="5">No items</td></tr>'}
            </tbody>
        </table>
        
        <div class="d-flex gap-2 mt-3">
            <button class="btn btn-success" onclick="approveBill(${bill.id})">Approve & Sign</button>
            <button class="btn btn-danger" onclick="rejectBill(${bill.id})">Reject</button>
            <button class="btn btn-secondary" onclick="closeModal('billDetailsModal')">Close</button>
        </div>
    `;
    
    document.getElementById('billDetailsModal').style.display = 'block';
}

async function approveBill(billId) {
    const remarks = prompt('Enter approval remarks (optional):') || '';
    
    if (confirm('Do you want to digitally sign this bill after approval?')) {
        const signatureType = prompt('Enter signature type (CLASS3_DSC_USB, CLASS3_DSC_PFX, AADHAAR_ESIGN):');
        
        if (!signatureType) {
            showAlert('Signature type is required', 'error');
            return;
        }
        
        try {
            // First approve
            await districtAPI.approveBill(billId, remarks);
            
            // Then sign
            const signatureData = {
                billId: billId,
                signatureType: signatureType,
                reAuthPassword: prompt('Enter password for re-authentication:') || ''
            };
            
            if (signatureType === 'AADHAAR_ESIGN') {
                const aadhaarNumber = prompt('Enter Aadhaar number (12 digits):');
                if (!aadhaarNumber || !/^\d{12}$/.test(aadhaarNumber)) {
                    showAlert('Invalid Aadhaar number', 'error');
                    return;
                }
                signatureData.aadhaarNumber = aadhaarNumber;
                
                // Generate OTP
                const otp = await otpAPI.generateAadhaarOtp(aadhaarNumber);
                alert(`OTP Generated (DUMMY): ${otp}`);
                signatureData.otp = prompt('Enter OTP:') || '';
            } else if (signatureType === 'CLASS3_DSC_PFX') {
                signatureData.certificatePassword = prompt('Enter certificate password:') || '';
            }
            
            await districtAPI.signBill(signatureData);
            showAlert('Bill approved and signed successfully!', 'success');
            closeModal('billDetailsModal');
            loadPendingBills();
        } catch (error) {
            showAlert(error.message, 'error');
        }
    } else {
        try {
            await districtAPI.approveBill(billId, remarks);
            showAlert('Bill approved successfully!', 'success');
            closeModal('billDetailsModal');
            loadPendingBills();
        } catch (error) {
            showAlert(error.message, 'error');
        }
    }
}

async function rejectBill(billId) {
    const remarks = prompt('Enter rejection remarks (mandatory):');
    
    if (!remarks || remarks.trim() === '') {
        showAlert('Rejection remarks are mandatory', 'error');
        return;
    }
    
    if (confirm('Are you sure you want to reject this bill?')) {
        try {
            await districtAPI.rejectBill(billId, remarks);
            showAlert('Bill rejected successfully', 'success');
            closeModal('billDetailsModal');
            loadPendingBills();
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