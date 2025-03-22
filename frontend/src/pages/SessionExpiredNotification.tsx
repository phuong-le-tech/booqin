import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const SessionExpiredNotification: React.FC = () => {
    const { isSessionExpired, resetSessionExpiredNotification } = useAuth();
    const [visible, setVisible] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        if (isSessionExpired) {
            setVisible(true);
        }
    }, [isSessionExpired]);

    const handleLogin = () => {
        resetSessionExpiredNotification();
        setVisible(false);

        // Save current location for post-login redirect
        localStorage.setItem('redirectAfterLogin', window.location.pathname);
        navigate('/login?session=expired');
    };

    const handleDismiss = () => {
        resetSessionExpiredNotification();
        setVisible(false);
    };

    if (!visible) return null;

    return (
        <div className="fixed inset-x-0 bottom-0 z-50 px-4 pb-4">
            <div className="pointer-events-auto w-full max-w-md mx-auto bg-white rounded-lg shadow-lg border border-red-100 overflow-hidden">
                <div className="p-4 bg-red-50">
                    <div className="flex items-start">
                        <div className="ml-3 w-0 flex-1">
                            <p className="text-sm font-medium text-red-800">
                                Your session has expired
                            </p>
                            <p className="mt-1 text-sm text-red-700">
                                Please log in again to continue.
                            </p>
                            <div className="mt-4 flex">
                                <button
                                    type="button"
                                    onClick={handleLogin}
                                    className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                                >
                                    Log in
                                </button>
                                <button
                                    type="button"
                                    onClick={handleDismiss}
                                    className="ml-3 inline-flex items-center px-3 py-2 border border-gray-300 text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                                >
                                    Dismiss
                                </button>
                            </div>
                        </div>
                        <div className="ml-4 flex-shrink-0 flex">
                            <button
                                className="bg-red-50 rounded-md inline-flex text-red-400 hover:text-red-500 focus:outline-none"
                                onClick={handleDismiss}
                            >
                                <span className="sr-only">Close</span>
                                <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SessionExpiredNotification;