// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands';

// Alternatively you can use CommonJS syntax:
// require('./commands')

// 全局配置
Cypress.on('uncaught:exception', (err, _runnable) => {
  // 忽略某些预期的错误
  if (err.message.includes('ResizeObserver loop limit exceeded')) {
    return false;
  }
  if (err.message.includes('Non-Error promise rejection captured')) {
    return false;
  }
  // 让其他错误继续抛出
  return true;
});

// 设置默认视口
Cypress.Commands.add('setViewport', (size: 'mobile' | 'tablet' | 'desktop' = 'desktop') => {
  const viewports = {
    mobile: [375, 667],
    tablet: [768, 1024],
    desktop: [1280, 720],
  };
  const [width, height] = viewports[size];
  cy.viewport(width, height);
});

// 等待页面加载完成
Cypress.Commands.add('waitForPageLoad', () => {
  cy.window().should('have.property', 'document');
  cy.document().should('have.property', 'readyState', 'complete');
});

// 等待API响应
Cypress.Commands.add('waitForApi', (alias: string, timeout = 10000) => {
  cy.wait(alias, { timeout });
});

// 清理本地存储
Cypress.Commands.add('clearStorage', () => {
  cy.clearLocalStorage();
  cy.clearCookies();
  cy.window().then((win) => {
    win.sessionStorage.clear();
  });
});

// 模拟网络延迟
Cypress.Commands.add('simulateNetworkDelay', (delay = 1000) => {
  cy.intercept('**', (req) => {
    req.reply((res) => {
      return new Promise((resolve) => {
        setTimeout(() => resolve(res), delay);
      });
    });
  });
});

// 检查控制台错误
Cypress.Commands.add('checkConsoleErrors', () => {
  cy.window().then((win) => {
    const errors: string[] = [];
    const originalError = win.console.error;

    win.console.error = (...args: any[]) => {
      errors.push(args.join(' '));
      originalError.apply(win.console, args);
    };

    cy.wrap(errors).should('have.length', 0);
  });
});

// 截图并保存
Cypress.Commands.add('takeScreenshot', (name?: string) => {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
  const screenshotName = name ? `${name}-${timestamp}` : `screenshot-${timestamp}`;
  cy.screenshot(screenshotName, { capture: 'fullPage' });
});

// 等待元素可见并可交互
Cypress.Commands.add('waitForElement', (selector: string, timeout = 10000) => {
  cy.get(selector, { timeout })
    .should('be.visible')
    .should('not.be.disabled');
});

// 安全点击（等待元素可点击）
Cypress.Commands.add('safeClick', (selector: string, options?: any) => {
  cy.waitForElement(selector);
  cy.get(selector).click(options);
});

// 安全输入（清空后输入）
Cypress.Commands.add('safeType', (selector: string, text: string, options?: any) => {
  cy.waitForElement(selector);
  cy.get(selector).clear().type(text, options);
});

// 等待加载完成
Cypress.Commands.add('waitForLoading', () => {
  // 等待加载指示器消失
  cy.get('[data-testid="loading"]', { timeout: 30000 }).should('not.exist');
  cy.get('.ant-spin', { timeout: 30000 }).should('not.exist');
});

// 检查页面可访问性
Cypress.Commands.add('checkA11y', () => {
  // 基本的可访问性检查
  cy.get('img').each(($img) => {
    cy.wrap($img).should('have.attr', 'alt');
  });

  cy.get('button').each(($btn) => {
    cy.wrap($btn).should('satisfy', ($el) => {
      return $el.text().trim() !== '' || $el.attr('aria-label') || $el.attr('title');
    });
  });
});

// 模拟拖拽
Cypress.Commands.add('dragAndDrop', (sourceSelector: string, targetSelector: string) => {
  cy.get(sourceSelector).trigger('mousedown', { button: 0 });
  cy.get(targetSelector).trigger('mousemove').trigger('mouseup');
});

// 等待WebSocket连接
Cypress.Commands.add('waitForWebSocket', () => {
  cy.window().its('WebSocket').should('exist');
  cy.wait(1000); // 等待连接建立
});

// 模拟文件上传
Cypress.Commands.add('uploadFile', (selector: string, fileName: string, fileType = 'application/json') => {
  cy.fixture(fileName).then((fileContent) => {
    cy.get(selector).selectFile({
      contents: Cypress.Buffer.from(JSON.stringify(fileContent)),
      fileName,
      mimeType: fileType,
    });
  });
});

// 检查响应式设计
Cypress.Commands.add('checkResponsive', () => {
  const viewports = [
    {
      name: 'mobile',
      width: 375,
      height: 667
    },
    {
      name: 'tablet',
      width: 768,
      height: 1024
    },
    {
      name: 'desktop',
      width: 1280,
      height: 720
    },
  ];

  viewports.forEach((viewport) => {
    cy.viewport(viewport.width, viewport.height);
    cy.wait(500); // 等待布局调整
    cy.takeScreenshot(`responsive-${viewport.name}`);
  });
});

// 性能监控
Cypress.Commands.add('measurePerformance', () => {
  cy.window().then((win) => {
    const performance = win.performance;
    const timing = performance.timing;

    const metrics = {
      domContentLoaded: timing.domContentLoadedEventEnd - timing.navigationStart,
      loadComplete: timing.loadEventEnd - timing.navigationStart,
      firstPaint: performance.getEntriesByType('paint')[0]?.startTime || 0,
    };

    cy.log('Performance Metrics:', metrics);

    // 断言性能指标
    expect(metrics.domContentLoaded).to.be.lessThan(3000); // 3秒内DOM加载完成
    expect(metrics.loadComplete).to.be.lessThan(5000); // 5秒内页面完全加载
  });
});

// 声明自定义命令的类型
declare global {
  namespace Cypress {
    interface Chainable {
      setViewport(size?: 'mobile' | 'tablet' | 'desktop'): Chainable<void>;
      waitForPageLoad(): Chainable<void>;
      waitForApi(alias: string, timeout?: number): Chainable<void>;
      clearStorage(): Chainable<void>;
      simulateNetworkDelay(delay?: number): Chainable<void>;
      checkConsoleErrors(): Chainable<void>;
      takeScreenshot(name?: string): Chainable<void>;
      waitForElement(selector: string, timeout?: number): Chainable<void>;
      safeClick(selector: string, options?: any): Chainable<void>;
      safeType(selector: string, text: string, options?: any): Chainable<void>;
      waitForLoading(): Chainable<void>;
      checkA11y(): Chainable<void>;
      dragAndDrop(sourceSelector: string, targetSelector: string): Chainable<void>;
      waitForWebSocket(): Chainable<void>;
      uploadFile(selector: string, fileName: string, fileType?: string): Chainable<void>;
      checkResponsive(): Chainable<void>;
      measurePerformance(): Chainable<void>;
    }
  }
}