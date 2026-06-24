/**
 * Основные скрипты системы управления проектами
 * @author Евдокимов Д.А.
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('ProjectSystem initialized');

    // Автозакрытие уведомлений
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Подтверждение удаления
    const deleteButtons = document.querySelectorAll('.btn-delete');
    deleteButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            if (!confirm('Вы уверены, что хотите удалить этот элемент?')) {
                e.preventDefault();
            }
        });
    });

    // Автозаполнение даты дедлайна (минимум сегодня)
    const deadlineInputs = document.querySelectorAll('input[type="date"][name="deadline"]');
    const today = new Date().toISOString().split('T')[0];
    deadlineInputs.forEach(function(input) {
        input.setAttribute('min', today);
    });

    // Подсветка просроченных задач
    const deadlineCells = document.querySelectorAll('td[data-deadline]');
    const currentDate = new Date();
    deadlineCells.forEach(function(cell) {
        const deadline = new Date(cell.dataset.deadline);
        if (deadline < currentDate) {
            cell.classList.add('table-danger');
        }
    });
});

/**
 * Форматирование даты
 */
function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('ru-RU', options);
}

/**
 * Показ уведомления
 */
function showNotification(message, type = 'info') {
    const container = document.querySelector('.container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    container.insertBefore(alert, container.firstChild);

    setTimeout(() => alert.remove(), 5000);
}