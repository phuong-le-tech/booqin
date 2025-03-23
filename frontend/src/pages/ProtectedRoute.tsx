import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useRequireAuth } from '../context/AuthContext';
import { Loader } from 'lucide-react';
import { Layout } from './Layout';

interface ProtectedRouteProps {
    children: React.ReactNode;
    adminOnly?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
                                                                  children,
                                                                  adminOnly = false
                                                              }) => {
    const { isLoading, isAuthenticated, hasAccess } = useRequireAuth(adminOnly);
    const location = useLocation();

    // Show loading while checking authentication status
    if (isLoading) {
        return (
            <Layout>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="flex flex-col items-center space-y-4">
                        <Loader className="h-10 w-10 text-blue-500 animate-spin" />
                        <p className="text-lg font-medium text-gray-700">
                            Verifying your session...
                        </p>
                    </div>
                </div>
            </Layout>
        );
    }

    // Not authenticated - redirect to login
    if (!isAuthenticated) {
        // Save current location for redirect after login
        localStorage.setItem('redirectAfterLogin', location.pathname);
        return <Navigate to="/login" replace />;
    }

    // Authenticated but no admin access
    if (adminOnly && !hasAccess) {
        return (
            <Layout>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded max-w-lg">
                        <h2 className="text-xl font-bold mb-2">Access Denied</h2>
                        <p>You don't have permission to access this page. This area is restricted to administrators only.</p>
                    </div>
                </div>
            </Layout>
        );
    }

    // All good - render the protected content
    return <>{children}</>;
};

export default ProtectedRoute;