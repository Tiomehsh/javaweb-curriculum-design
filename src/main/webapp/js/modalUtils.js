// modalUtils.js

/**
 * 打开指定的模态框
 * @param {string} modalId - 模态框的 ID
 */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        console.error('Modal with id "' + modalId + '" not found.');
        return;
    }

    modal.style.display = 'flex'; // 使用 flex 布局以利用 align-items 和 justify-content 进行居中

    // 强制浏览器重绘/回流，以确保 CSS 过渡在添加 'show' 类时正确应用
    void modal.offsetWidth; // 这是一个常用的技巧

    modal.classList.add('show');

    // 添加事件监听器，用于处理 ESC 键和点击外部关闭模态框
    document.addEventListener('keydown', handleEscKey);
    // 为当前打开的模态框添加特定的点击外部关闭监听
    modal.addEventListener('click', handleClickOutside);
}

/**
 * 关闭指定的模态框
 * @param {string} modalId - 模态框的 ID
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        console.error('Modal with id "' + modalId + '" not found.');
        return;
    }

    modal.classList.remove('show');

    // 等待 CSS 过渡动画完成 (0.2s, 即 200ms)
    // 这个时间应与 CSS 中 .modal-content 的 transition-duration 匹配
    setTimeout(() => {
        modal.style.display = 'none';
    }, 200);

    // 移除事件监听器
    document.removeEventListener('keydown', handleEscKey);
    // 移除特定于此模态框的点击外部关闭监听
    modal.removeEventListener('click', handleClickOutside);
}

/**
 * 全局处理 ESC 键按下事件，关闭所有打开的模态框
 * @param {KeyboardEvent} event
 */
function handleEscKey(event) {
    if (event.key === 'Escape') {
        const openModals = document.querySelectorAll('.modal.show');
        openModals.forEach(modal => {
            // 调用 closeModal 会移除自身的监听器，所以这里是安全的
            closeModal(modal.id);
        });
    }
}

/**
 * 处理点击模态框外部区域事件，关闭模态框
 * @param {MouseEvent} event
 */
function handleClickOutside(event) {
    // event.currentTarget 指向的是监听器附加到的元素 (即 .modal)
    // event.target 指向的是实际点击的元素
    if (event.target === event.currentTarget) {
        closeModal(event.currentTarget.id);
    }
}