// sidebar.js - 侧边栏收起/展开功能

/**
 * 切换侧边栏的收起/展开状态
 */
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');
    
    if (!sidebar || !mainContent) {
        console.error('Sidebar or main content element not found');
        return;
    }
    
    // 切换CSS类
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
    
    // 保存状态到localStorage
    const isCollapsed = sidebar.classList.contains('collapsed');
    localStorage.setItem('sidebarCollapsed', isCollapsed);
}

/**
 * 初始化侧边栏状态
 * 从localStorage读取之前保存的状态
 */
function initSidebarState() {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');
    
    if (!sidebar || !mainContent) {
        return;
    }
    
    // 从localStorage读取状态
    const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    
    if (isCollapsed) {
        sidebar.classList.add('collapsed');
        mainContent.classList.add('expanded');
    }
}

/**
 * 设置当前页面的导航项为激活状态
 */
function setActiveNavItem() {
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        item.classList.remove('active');
        
        // 获取链接的href属性
        const href = item.getAttribute('href');
        if (href && currentPath.includes(href)) {
            item.classList.add('active');
        }
    });
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initSidebarState();
    setActiveNavItem();
});

// 响应式处理：在小屏幕上自动收起侧边栏
function handleResponsiveSidebar() {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');
    
    if (!sidebar || !mainContent) {
        return;
    }
    
    if (window.innerWidth <= 1024) {
        // 小屏幕时自动收起
        sidebar.classList.add('collapsed');
        mainContent.classList.add('expanded');
    } else {
        // 大屏幕时恢复保存的状态
        const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
        if (isCollapsed) {
            sidebar.classList.add('collapsed');
            mainContent.classList.add('expanded');
        } else {
            sidebar.classList.remove('collapsed');
            mainContent.classList.remove('expanded');
        }
    }
}

// 监听窗口大小变化
window.addEventListener('resize', handleResponsiveSidebar);
