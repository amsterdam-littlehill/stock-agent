import { memo, useEffect, useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  ConfigProvider,
  message,
  Layout as AntLayout,
  Menu,
  Button,
  Typography,
  Space,
  Drawer
} from 'antd';
import {
  DashboardOutlined,
  SearchOutlined,
  BarChartOutlined,
  HomeOutlined,
  MenuOutlined,
  ApartmentOutlined,
  PlayCircleOutlined,
  SettingOutlined,
  TeamOutlined,
  FundOutlined
} from '@ant-design/icons';
import { StockOutlined } from '@ant-design/icons';
import { ConstantProvider } from '@/hooks';
import * as constants from "@/utils/constants";
import { setMessage } from '@/utils';

const { Header, Sider, Content } = AntLayout;
const { Title } = Typography;

// Layout 组件：应用的主要布局结构
const Layout: GenieType.FC = memo(() => {
  const [messageApi, messageContent] = message.useMessage();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileMenuVisible, setMobileMenuVisible] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // 初始化全局 message
    setMessage(messageApi);
  }, [messageApi]);

  // 菜单项配置
  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页',
      onClick: () => navigate('/')
    },
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '仪表板',
      onClick: () => navigate('/dashboard')
    },
    {
      key: 'stock',
      icon: <BarChartOutlined />,
      label: '股票分析',
      children: [
        {
          key: '/stock-search',
          icon: <SearchOutlined />,
          label: '股票搜索',
          onClick: () => navigate('/stock-search')
        },
        {
          key: '/stock-analysis',
          icon: <BarChartOutlined />,
          label: '股票分析',
          onClick: () => navigate('/stock-analysis')
        },
        {
          key: '/portfolio',
          icon: <FundOutlined />,
          label: '投资组合',
          onClick: () => navigate('/portfolio')
        }
      ]
    },
    {
      key: '/collaboration',
      icon: <TeamOutlined />,
      label: '智能体协作',
      onClick: () => navigate('/collaboration')
    },
    {
      key: 'workflow',
      icon: <ApartmentOutlined />,
      label: '工作流',
      children: [
        {
          key: '/workflow',
          icon: <SettingOutlined />,
          label: '工作流管理',
          onClick: () => navigate('/workflow')
        },
        {
          key: '/workflow/designer/new',
          icon: <PlayCircleOutlined />,
          label: '创建工作流',
          onClick: () => navigate('/workflow/designer/new')
        }
      ]
    }
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    const item = menuItems.find(item => item.key === key);
    if (item?.onClick) {
      item.onClick();
      setMobileMenuVisible(false);
    }
  };

  // 移动端菜单
  const MobileMenu = (
    <Menu
      mode="vertical"
      selectedKeys={[location.pathname]}
      items={menuItems}
      onClick={handleMenuClick}
      style={{ border: 'none' }}
    />
  );

  return (
    <ConfigProvider theme={{
      token: {
        colorPrimary: '#1890ff',
        borderRadius: 6
      }
    }}>
      {messageContent}
      <ConstantProvider value={constants}>
        <AntLayout style={{ minHeight: '100vh' }}>
          {/* 桌面端侧边栏 */}
          <Sider
            trigger={null}
            collapsible
            collapsed={collapsed}
            breakpoint="lg"
            collapsedWidth={0}
            style={{
              background: '#fff',
              boxShadow: '2px 0 8px rgba(0,0,0,0.1)'
            }}
            className="desktop-sider"
          >
            <div style={{
              padding: '16px',
              borderBottom: '1px solid #f0f0f0',
              textAlign: 'center'
            }}>
              <Space align="center">
                <StockOutlined style={{
                  fontSize: '24px',
                  color: '#1890ff'
                }} />
                {!collapsed && (
                  <Title level={4} style={{
                    margin: 0,
                    color: '#1890ff'
                  }}>
                    Agent Genie
                  </Title>
                )}
              </Space>
            </div>
            <Menu
              mode="inline"
              selectedKeys={[location.pathname]}
              items={menuItems}
              onClick={handleMenuClick}
              style={{
                border: 'none',
                marginTop: '8px'
              }}
            />
          </Sider>

          <AntLayout>
            {/* 头部 */}
            <Header style={{
              background: '#fff',
              padding: '0 24px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <Space>
                {/* 桌面端折叠按钮 */}
                <Button
                  type="text"
                  icon={<MenuOutlined />}
                  onClick={() => setCollapsed(!collapsed)}
                  className="desktop-menu-trigger"
                  style={{ display: 'none' }}
                />

                {/* 移动端菜单按钮 */}
                <Button
                  type="text"
                  icon={<MenuOutlined />}
                  onClick={() => setMobileMenuVisible(true)}
                  className="mobile-menu-trigger"
                />

                <Title level={3} style={{
                  margin: 0,
                  color: '#333'
                }}>
                  Stock Agent Genie
                </Title>
              </Space>
            </Header>

            {/* 主内容区域 */}
            <Content style={{
              margin: 0,
              background: '#f5f5f5',
              overflow: 'auto'
            }}>
              <Outlet />
            </Content>
          </AntLayout>

          {/* 移动端抽屉菜单 */}
          <Drawer
            title={(
              <Space>
                <StockOutlined style={{ color: '#1890ff' }} />
                <span>Stock Agent Genie</span>
              </Space>
            )}
            placement="left"
            onClose={() => setMobileMenuVisible(false)}
            open={mobileMenuVisible}
            styles={{ body: { padding: 0 } }}
            width={280}
          >
            {MobileMenu}
          </Drawer>
        </AntLayout>
      </ConstantProvider>

      <style>{`
        @media (min-width: 992px) {
          .mobile-menu-trigger {
            display: none !important;
          }
          .desktop-menu-trigger {
            display: inline-flex !important;
          }
          .desktop-sider {
            display: block !important;
          }
        }
        
        @media (max-width: 991px) {
          .desktop-sider {
            display: none !important;
          }
        }
        
        .ant-layout-sider-children {
          display: flex;
          flex-direction: column;
        }
        
        .ant-menu {
          flex: 1;
        }
        
        .ant-menu-item {
          margin: 4px 8px !important;
          border-radius: 6px !important;
        }
        
        .ant-menu-item-selected {
          background: #e6f7ff !important;
          color: #1890ff !important;
        }
        
        .ant-menu-item:hover {
          background: #f5f5f5 !important;
        }
      `}</style>
    </ConfigProvider>
  );
});

Layout.displayName = 'Layout';

export default Layout;
