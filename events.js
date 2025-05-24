document.addEventListener('DOMContentLoaded', function() {
    // Initialize event-specific functionality
    initializeEventCards();

    // Set up search form handling
    const searchForm = document.querySelector('form[action="/events/search"]');
    if (searchForm) {
        searchForm.addEventListener('submit', handleSearch);
    }
});

/**
 * Apply filters and redirect to search results
 */
function applyFilters() {
    // Get filter values
    const startDate = document.getElementById('dateRangeStart').value;
    const endDate = document.getElementById('dateRangeEnd').value;
    const minPrice = document.getElementById('minPrice').value ? parseFloat(document.getElementById('minPrice').value) : null;
    const maxPrice = document.getElementById('maxPrice').value ? parseFloat(document.getElementById('maxPrice').value) : null;

    // Apply client-side filtering immediately
    filterByDateRange(startDate, endDate);
    filterByPriceRange(minPrice, maxPrice);

    // Build query string for server-side filtering
    let queryParams = [];
    if (startDate) queryParams.push(`dateRangeStart=${encodeURIComponent(startDate)}`);
    if (endDate) queryParams.push(`dateRangeEnd=${encodeURIComponent(endDate)}`);
    if (minPrice) queryParams.push(`minPrice=${encodeURIComponent(minPrice)}`);
    if (maxPrice) queryParams.push(`maxPrice=${encodeURIComponent(maxPrice)}`);

    // Redirect with query parameters
    const url = queryParams.length > 0 ? `/events/filter?${queryParams.join('&')}` : '/events';
    fetch(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                window.location.href = url;
                showToast('Filters applied successfully', 'success');
            } else {
                showToast('Failed to apply filters', 'danger');
            }
        })
        .catch(error => {
            console.error('Error applying filters:', error);
            showToast('Error applying filters', 'danger');
        });
}

/**
 * Initialize event cards with hover effects and interactions
 */
function initializeEventCards() {
    const eventCards = document.querySelectorAll('.card');

    eventCards.forEach(card => {
        // Add hover effect
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 10px 20px rgba(0, 0, 0, 0.12)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
        });
    });
}

/**
 * Handle search form submission
 */
function handleSearch(event) {
    const searchInput = event.target.querySelector('input[name="keyword"]');
    if (searchInput && searchInput.value.trim() === '') {
        event.preventDefault();
        showToast('Please enter a search term', 'warning');
    }
}

/**
 * Filter events by price range (client-side)
 * @param {number} minPrice - Minimum price
 * @param {number} maxPrice - Maximum price
 */
function filterByPriceRange(minPrice, maxPrice) {
    const eventCards = document.querySelectorAll('.card');

    eventCards.forEach(card => {
        const priceElement = card.querySelector('.badge');
        if (priceElement) {
            const price = parseFloat(priceElement.textContent.replace('$', ''));

            if ((minPrice === null || price >= minPrice) &&
                (maxPrice === null || price <= maxPrice)) {
                card.closest('.col').style.display = 'block';
            } else {
                card.closest('.col').style.display = 'none';
            }
        }
    });
}

/**
 * Filter events by date range (client-side)
 * @param {string} startDate - Start date in ISO format
 * @param {string} endDate - End date in ISO format
 */
function filterByDateRange(startDate, endDate) {
    const eventCards = document.querySelectorAll('.card');

    const startDateObj = startDate ? new Date(startDate) : null;
    const endDateObj = endDate ? new Date(endDate) : null;

    eventCards.forEach(card => {
        const dateElement = card.querySelector('.text-muted span');
        if (dateElement) {
            const dateStr = dateElement.textContent;
            const dateObj = new Date(dateStr);

            if ((startDateObj === null || dateObj >= startDateObj) &&
                (endDateObj === null || dateObj <= endDateObj)) {
                card.closest('.col').style.display = 'block';
            } else {
                card.closest('.col').style.display = 'none';
            }
        }
    });
}

/**
 * Sort events by date (server-side)
 */
function sortByDate() {
    fetch('/events/sort?sortBy=date', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                window.location.href = '/events?sortBy=date';
                showToast('Events sorted by date', 'success');
            } else {
                showToast('Failed to sort by date', 'danger');
            }
        })
        .catch(error => {
            console.error('Error sorting by date:', error);
            showToast('Error sorting by date', 'danger');
        });
}

/**
 * Sort events by price (server-side)
 * @param {string} order - 'asc' or 'desc'
 */
function sortByPrice(order) {
    fetch(`/events/sort?sortBy=price&order=${order}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                window.location.href = `/events?sortBy=price&order=${order}`;
                showToast(`Events sorted by price (${order === 'asc' ? 'low to high' : 'high to low'})`, 'success');
            } else {
                showToast(`Failed to sort by price (${order})`, 'danger');
            }
        })
        .catch(error => {
            console.error(`Error sorting by price (${order}):`, error);
            showToast(`Error sorting by price (${order})`, 'danger');
        });
}

/**
 * Sort events by popularity (server-side)
 */
function sortByPopularity() {
    fetch('/events/sort?sortBy=popularity', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                window.location.href = '/events?sortBy=popularity';
                showToast('Events sorted by popularity', 'success');
            } else {
                showToast('Failed to sort by popularity', 'danger');
            }
        })
        .catch(error => {
            console.error('Error sorting by popularity:', error);
            showToast('Error sorting by popularity', 'danger');
        });
}

/**
 * Reset filters and redirect to all events
 */
function resetFilters() {
    const form = document.getElementById('filterForm');
    if (form) form.reset();
    fetch('/events', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.ok) {
                window.location.href = '/events';
                showToast('Filters reset', 'success');
            } else {
                showToast('Failed to reset filters', 'danger');
            }
        })
        .catch(error => {
            console.error('Error resetting filters:', error);
            showToast('Error resetting filters', 'danger');
        });
}

/**
 * Create a favorite event (client-side only)
 * @param {string} eventId - The event ID
 */
function toggleFavorite(eventId) {
    const button = document.querySelector(`.favorite-btn[data-event-id="${eventId}"]`);
    if (button) {
        const isFavorite = button.classList.contains('active');

        if (isFavorite) {
            button.classList.remove('active');
            button.innerHTML = '<i class="far fa-heart"></i>';
            showToast('Removed from favorites', 'warning');
        } else {
            button.classList.add('active');
            button.innerHTML = '<i class="fas fa-heart"></i>';
            showToast('Added to favorites', 'success');
        }
    }
}

/**
 * Show a toast notification
 * @param {string} message - Message to display
 * @param {string} type - 'success', 'warning', 'danger'
 */
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        const newContainer = document.createElement('div');
        newContainer.id = 'toastContainer';
        newContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(newContainer);
    }

    const toastClasses = {
        success: 'bg-success',
        warning: 'bg-warning',
        danger: 'bg-danger'
    };

    const toastHTML = `
        <div class="toast align-items-center text-white border-0" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="3000">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `.replace('bg-success', toastClasses[type] || 'bg-success');

    document.getElementById('toastContainer').innerHTML += toastHTML;

    const toastElList = document.querySelectorAll('.toast');
    const toastList = [...toastElList].map(toastEl => new bootstrap.Toast(toastEl));
    toastList.forEach(toast => toast.show());
}