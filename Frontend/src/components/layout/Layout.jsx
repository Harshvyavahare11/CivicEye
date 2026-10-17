import React from 'react';
import Navbar from '../common/Navbar';
import Footer from '../common/Footer';
import useServiceHealth from '../../hooks/useServiceHealth';

export function Layout({ children }) {
  const { backendHealth, aiHealth } = useServiceHealth(15000);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar backendHealth={backendHealth} aiHealth={aiHealth} />
      <main style={{ flex: 1 }}>
        {React.Children.map(children, child => {
          if (React.isValidElement(child)) {
            return React.cloneElement(child, { backendHealth, aiHealth });
          }
          return child;
        })}
      </main>
      <Footer />
    </div>
  );
}

export default Layout;
