import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: true,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
    pageLoadTimeout: 30000,
    experimentalStudio: true,
    setupNodeEvents(on, config) {
      // 实现节点事件监听器
      on('task', {
        log(message) {
          console.log(message);
          return null;
        },
        table(message) {
          console.table(message);
          return null;
        },
      });

      // 环境变量配置
      config.env = {
        ...config.env,
        API_BASE_URL: process.env.CYPRESS_API_BASE_URL || 'http://localhost:8080/api/v1',
        WS_BASE_URL: process.env.CYPRESS_WS_BASE_URL || 'ws://localhost:8080/ws',
        TEST_USER_EMAIL: process.env.CYPRESS_TEST_USER_EMAIL || 'test@example.com',
        TEST_USER_PASSWORD: process.env.CYPRESS_TEST_USER_PASSWORD || 'password123',
      };

      return config;
    },
  },
  component: {
    devServer: {
      framework: 'create-react-app',
      bundler: 'webpack',
    },
    supportFile: 'cypress/support/component.ts',
    specPattern: 'src/**/*.cy.{js,jsx,ts,tsx}',
    indexHtmlFile: 'cypress/support/component-index.html',
  },
  retries: {
    runMode: 2,
    openMode: 0,
  },
  env: {coverage: true,},
});