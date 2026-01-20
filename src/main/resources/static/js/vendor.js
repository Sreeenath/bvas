// src/main/resources/static/js/vendor.js

let selectedFiles = [];
let districts = [];

// Load districts (you'll need to create an endpoint or hardcode)
const UTTARAKHAND_DISTRICTS = [
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

districts = UTTARAKHAND_DISTRICTS;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    if (currentUser.role !== 'VENDOR') {
        window.location.href = '/login.html';
        return;
    }
    
    loadBills();
    initializeBillItems();
});

function initializeBillItems() {
    const container = document.getElementById('billItemsContainer');
    container.innerHTML = '';
    districts.forEach(district => {
        addBillItemRow(district);
    });
}

function addBillItemRow(district) {
    const container = document.getElementById('billItemsContainer');
    const row = document.createElement('div');
    row.className = 'form-group';
    row.innerHTML = `
        <label class="form-label">${district.name} (${district.code})</label>
        <div class="d-flex gap-2">
            <input type="number" class="form-control" 
                   data-district-id="${district.id}" 
                   placeholder="Quantity" 
                   step="0.01" 
                   min="0" 
                   required>
            <input type="number" class="form-control" 
                   data-district-id="${district.id}" 
                   placeholder="Amount" 
                   step="0.01" 
                   min="0">
        </div>
    `;
    container.appendChild(row);
}

function addBillItem() {
    // Already showing all districts
    showAlert('All districts are already included', 'info');
}

function handleFileSelect(event) {
    selectedFiles = Array.from(event.target.files);
    const fileList = document.getElementById('fileList');
    fileList.innerHTML = selectedFiles.map((file, index) => 
        `<div class="mt-1">${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)</div>`
    ).join('');
}

function showSubmitBill() {
    document.getElementById('submitBillForm').reset();
    selectedFiles = [];
    document.getElementById('fileList').innerHTML = '';
    document.getElementById('billYear').value = new Date().getFullYear();
    document.getElementById('submitBillModal').style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

async function loadBills() {
    const container = document.getElementById('billsTableContainer');
    container.innerHTML = '<div class="spinner"></div>';
    
    try {
        const bills = await vendorAPI.getBills();
        displayBills(bills);
    } catch (error) {
        container.innerHTML = `<div class="alert alert-error">${error.message}</div>`;
    }
}

function displayBills(bills) {
    const container = document.getElementById('billsTableContainer');
    
    if (bills.length === 0) {
        container.innerHTML = '<p class="text-center">No bills found</p>';
        return;
    }
    
    const table = `
        <table class="table">
            <thead>
                <tr>
                    <th>Bill ID</th>
                    <th>Month/Year</th>
                    <th>Status</th>
                    <th>Total Amount</th>
                    <th>Submitted At</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${bills.map(bill => `
                    <tr>
                        <td>${bill.id}</td>
                        <td>${bill.billMonth}/${bill.billYear}</td>
                        <td>${getStatusBadge(bill.status)}</td>
                        <td>₹${bill.totalAmount || '0.00'}</td>
                        <td>${formatDate(bill.submittedAt)}</td>
                        <td>
                            <button class="btn btn-sm btn-primary" onclick="viewBill(${bill.id})">View</button>
                            ${bill.status === 'REJECTED' ? 
                                `<button class="btn btn-sm btn-warning" onclick="resubmitBill(${bill.id})">Resubmit</button>` : 
                                ''}
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    
    container.innerHTML = table;
}

async function viewBill(billId) {
    try {
        const bill = await vendorAPI.getBill(billId);
        // Show bill details in modal
        alert(`Bill Details:\nMonth/Year: ${bill.billMonth}/${bill.billYear}\nStatus: ${bill.status}\nTotal: ₹${bill.totalAmount || '0.00'}`);
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

async function resubmitBill(billId) {
    if (confirm('Are you sure you want to resubmit this bill?')) {
        showSubmitBill();
        // Pre-fill form with existing bill data
    }
}

document.getElementById('submitBillForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    try {
        const billItems = [];
        document.querySelectorAll('[data-district-id]').forEach((input, index) => {
            if (index % 2 === 0) { // Quantity input
                const districtId = input.getAttribute('data-district-id');
                const quantity = parseFloat(input.value);
                const amountInput = input.nextElementSibling;
                const amount = parseFloat(amountInput.value) || 0;
                
                if (quantity > 0) {
                    billItems.push({
                        districtId: parseInt(districtId),
                        quantity: quantity,
                        amount: amount
                    });
                }
            }
        });
        
        if (billItems.length === 0) {
            showAlert('Please enter quantities for at least one district', 'error');
            return;
        }
        
        const billData = {
            billMonth: parseInt(document.getElementById('billMonth').value),
            billYear: parseInt(document.getElementById('billYear').value),
            vendorRemarks: document.getElementById('vendorRemarks').value,
            billItems: billItems
        };
        
        const result = await vendorAPI.submitBill(billData, selectedFiles);
        showAlert('Bill submitted successfully!', 'success');
        closeModal('submitBillModal');
        loadBills();
    } catch (error) {
        showAlert(error.message, 'error');
    }
});

// Close modal on outside click
window.onclick = function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
}