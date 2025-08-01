import React, { Suspense } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import Layout from '@/layout/index';
import { Loading } from '@/components';

// 使用常量存储路由路径
const ROUTES = {
  HOME: '/',
  DASHBOARD: '/dashboard',
  STOCK_SEARCH: '/stock-search',
  STOCK_ANALYSIS: '/stock-analysis',
  PORTFOLIO: '/portfolio',
  WORKFLOW: '/workflow',
  WORKFLOW_DESIGNER: '/workflow/designer/:id',
  WORKFLOW_EXECUTION: '/workflow/execution/:executionId',
  WORKFLOW_DETAIL: '/workflow/detail/:id',
  COLLABORATION: '/collaboration',
  NOT_FOUND: '*',
};

// 使用 React.lazy 懒加载组件
const Home = React.lazy(() => import('@/pages/Home'));
const Dashboard = React.lazy(() => import('@/pages/Dashboard'));
const StockSearch = React.lazy(() => import('@/pages/StockSearch'));
const StockAnalysis = React.lazy(() => import('@/pages/StockAnalysis'));
const Portfolio = React.lazy(() => import('@/pages/Portfolio'));
const Workflow = React.lazy(() => import('@/pages/Workflow'));
const WorkflowDesigner = React.lazy(() => import('@/pages/WorkflowDesigner'));
const WorkflowExecution = React.lazy(() => import('@/pages/WorkflowExecution'));
const Collaboration = React.lazy(() => import('@/pages/Collaboration'));
const NotFound = React.lazy(() => import('@/components/NotFound'));

// 创建路由配置
const router = createBrowserRouter([
  {
    path: ROUTES.HOME,
    element: <Layout />,
    children: [
      {
        index: true,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <Home />
          </Suspense>
        ),
      },
      {
        path: ROUTES.DASHBOARD,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <Dashboard />
          </Suspense>
        ),
      },
      {
        path: ROUTES.STOCK_SEARCH,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <StockSearch />
          </Suspense>
        ),
      },
      {
        path: ROUTES.STOCK_ANALYSIS,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <StockAnalysis />
          </Suspense>
        ),
      },
      {
        path: ROUTES.PORTFOLIO,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <Portfolio />
          </Suspense>
        ),
      },
      {
        path: ROUTES.WORKFLOW,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <Workflow />
          </Suspense>
        ),
      },
      {
        path: ROUTES.WORKFLOW_DESIGNER,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <WorkflowDesigner />
          </Suspense>
        ),
      },
      {
        path: ROUTES.WORKFLOW_EXECUTION,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <WorkflowExecution />
          </Suspense>
        ),
      },
      {
        path: ROUTES.WORKFLOW_DETAIL,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <Workflow />
          </Suspense>
        ),
      },
      {
        path: ROUTES.COLLABORATION,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <Collaboration />
          </Suspense>
        ),
      },
      {
        path: ROUTES.NOT_FOUND,
        element: (
          <Suspense fallback={<Loading loading={true} className="h-full"/>}>
            <NotFound />
          </Suspense>
        ),
      },
    ],
  },
  // 重定向所有未匹配的路由到 404 页面
  {
    path: '*',
    element: <Navigate to={ROUTES.NOT_FOUND} replace />,
  },
]);

export default router;
