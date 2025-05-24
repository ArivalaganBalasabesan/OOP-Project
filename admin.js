/**
 * JavaScript for Admin Dashboard
 * Handles functionality specific to admin pages
 */

document.addEventListener('DOMContentLoaded', function() {
    setupDeleteEventModal();
    setupDeleteUserModal();
    setupCancelTicketModal();
    setupDataTables();
    setupMobileSidebar();
    setupFormValidations();
});

/**
 * Set up delete event modal
 */
function setupDeleteEventModal() {
    const deleteEventModal = document.getElementById('deleteEventModal');
    if (deleteEventModal) {
        deleteEventModal.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const eventId = button.getAttribute('data-event-id');
            const eventName = button.getAttribute('data-event-name');

            const modalForm = this.querySelector('form');
            modalForm.action = `/events/delete/${eventId}`;

            const eventNameEl = this.querySelector('.event-name');
            if (eventNameEl) {
                eventNameEl.textContent = eventName;
            }
        });
    }
}

/**
 * Set up delete user modal
 */
function setupDeleteUserModal() {
    const deleteUserModal = document.getElementById('deleteUserModal');
    if (deleteUserModal) {
        deleteUserModal.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const userId = button.getAttribute('data-user-id');
            const username = button.getAttribute('data-username');

            const modalForm = this.querySelector('form');
            modalForm.action = `/admin/users/delete/${userId}`;

            const usernameEl = this.querySelector('.username');
            if (usernameEl) {
                usernameEl.textContent = username;
            }
        });
    }
}

/**
 * Set up cancel ticket modal
 */
function setupCancelTicketModal() {
    const cancelTicketModal = document.getElementById('cancelTicketModal');
    if (cancelTicketModal) {
        cancelTicketModal.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const ticketId = button.getAttribute('data-ticket-id');
            const eventName = button.getAttribute('data-event-name');

            const modalForm = this.querySelector('form');
            modalForm.action = `/tickets/cancel/${ticketId}`;

            const eventNameEl = this.querySelector('.event-name');
            if (eventNameEl) {
                eventNameEl.textContent = eventName;
            }
        });
    }
}

/**
 * Set up DataTables for admin tables
 */
function setupDataTables() {
    const tables = document.querySelectorAll('.admin-table');

    tables.forEach(table => {
        // Sort table when header is clicked
        const headers = table.querySelectorAll('th[data-sortable="true"]');
        headers.forEach((header, index) => {
            header.addEventListener('click', function() {
                sortTable(table, index);

                // Update sort indicators
                const currentDirection = this.getAttribute('data-sort-direction') || 'asc';
                const newDirection = currentDirection === 'asc' ? 'desc' : 'asc';
                this.setAttribute('data-sort-direction', newDirection);

                updateSortIndicators(headers, index, newDirection);
            });
        });
    });
}

/**
 * Sort a table by the specified column index
 * @param {HTMLElement} table - The table element
 * @param {number} columnIndex - The index of the column to sort by
 */
function sortTable(table, columnIndex) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));
    const header = table.querySelectorAll('th')[columnIndex];
    const direction = header.getAttribute('data-sort-direction') === 'asc' ? 1 : -1;

    // Sort rows based on cell content in the specified column
    rows.sort((a, b) => {
        const cellA = a.querySelectorAll('td')[columnIndex].textContent.trim();
        const cellB = b.querySelectorAll('td')[columnIndex].textContent.trim();

        if (!isNaN(cellA) && !isNaN(cellB)) {
            return direction * (parseFloat(cellA) - parseFloat(cellB));
        } else {
            return direction * cellA.localeCompare(cellB);
        }
    });

    // Remove existing rows
    while (tbody.firstChild) {
        tbody.removeChild(tbody.firstChild);
    }

    // Add sorted rows
    rows.forEach(row => {
        tbody.appendChild(row);
    });
}

/**
 * Update the sort indicator icons in table headers
 * @param {NodeList} headers - The table headers
 * @param {number} activeIndex - The index of the active header
 * @param {string} direction - The sort direction ('asc' or 'desc')
 */
function updateSortIndicators(headers, activeIndex, direction) {
    headers.forEach((header, index) => {
        const icon = header.querySelector('i');

        if (index === activeIndex) {
            if (direction === 'asc') {
                icon.className = 'fas fa-sort-up';
            } else {
                icon.className = 'fas fa-sort-down';
            }
        } else {
            icon.className = 'fas fa-sort';
        }
    });
}

/**
 * Set up sidebar toggle for mobile view
 */
function setupMobileSidebar() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            document.getElementById('adminSidebar').classList.toggle('show');
        });
    }
}

/**
 * Set up form validations for admin forms
 */
function setupFormValidations() {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add('was-validated');
        });
    });
}

/**
 * Process the next ticket request
 */
function processNextTicket() {
    fetch('/admin/tickets/process-next', { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAdminAlert(data.message, 'success');
                // Reload the page to show updated data
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                showAdminAlert(data.message, 'danger');
            }
        })
        .catch(error => {
            showAdminAlert('An error occurred while processing the ticket request.', 'danger');
            console.error('Error:', error);
        });
}

/**
 * Process all pending ticket requests
 */
function processAllTickets() {
    fetch('/admin/tickets/process-all', { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAdminAlert(data.message, 'success');
                // Reload the page to show updated data
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                showAdminAlert(data.message, 'danger');
            }
        })
        .catch(error => {
            showAdminAlert('An error occurred while processing ticket requests.', 'danger');
            console.error('Error:', error);
        });
}

/**
 * Display an alert in the admin dashboard
 * @param {string} message - The message to display
 * @param {string} type - The alert type (success, danger, warning, info)
 */
function showAdminAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');
    if (alertContainer) {
        const alertElement = document.createElement('div');
        alertElement.className = `alert alert-${type} alert-dismissible fade show`;
        alertElement.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        alertContainer.innerHTML = '';
        alertContainer.appendChild(alertElement);

        // Auto dismiss after 5 seconds
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alertElement);
            bsAlert.close();
        }, 5000);
    }
}