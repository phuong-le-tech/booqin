import React from "react"
import {X} from "lucide-react"

interface DeleteConfirmModalProps {
    title: string
    message: string
    onConfirm: () => void
    onCancel: () => void
}

export const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({
                                                                          title,
                                                                          message,
                                                                          onConfirm,
                                                                          onCancel
                                                                      }) => {
    return (
        <>
            {/* Modal Backdrop */}
            <div
                className="fixed inset-0 bg-black/50 backdrop-blur-[1px] z-50 flex items-center justify-center"
                onClick={onCancel}
            >
                {/* Modal Content */}
                <div
                    className="bg-white rounded-lg shadow-lg max-w-md w-full p-6"
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-xl font-semibold text-red-600">{title}</h3>
                        <button
                            onClick={onCancel}
                            className="text-gray-400 hover:text-gray-600"
                        >
                            <X className="h-5 w-5"/>
                        </button>
                    </div>

                    <div className="mb-6">
                        <p className="text-gray-700">{message}</p>
                    </div>

                    <div className="flex justify-end space-x-3">
                        <button
                            onClick={onCancel}
                            className="px-4 py-2 rounded bg-gray-200 text-gray-800 hover:bg-gray-300"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={onConfirm}
                            className="px-4 py-2 rounded bg-red-600 text-white hover:bg-red-700"
                        >
                            Delete
                        </button>
                    </div>
                </div>
            </div>
        </>
    )
}

export default DeleteConfirmModal