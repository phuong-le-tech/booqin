import React, { useState, useContext } from "react";
import { useNavigate, Link } from "react-router-dom";
import Layout from "../../pages/Layout";
import BookCollectionService from "../../services/BookCollectionService";
import { useAuth } from "../../context/AuthContext";

const CRITERIA_TYPES = [
  { value: "AUTHOR", label: "Author Name" },
  { value: "GENRE", label: "Book Genre" },
  { value: "PUBLICATION_YEAR", label: "Publication Year" },
  { value: "TITLE_CONTAINS", label: "Title Contains" }
];

const CreateSmartCollection: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // États du formulaire
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(false);
  const [criteriaType, setCriteriaType] = useState(CRITERIA_TYPES[0].value);
  const [criteriaValue, setCriteriaValue] = useState("");

  // Gestion de la soumission du formulaire
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Vérifications de base
    if (!user) {
      setError("You must be logged in to create a collection");
      return;
    }

    if (!title.trim()) {
      setError("Title is required");
      return;
    }

    if (!criteriaValue.trim()) {
      setError("Criteria value is required");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Créer une collection avec les informations de l'utilisateur
      const data = {
        title: title + " (Smart)",  // Ajout de "(Smart)" pour identifier dans la liste
        description,
        isPublic,
        userId: user.id,  // Explicitement définir l'ID de l'utilisateur
        criteria: {
          type: criteriaType,
          value: criteriaValue
        }
      };

      // Créer la collection
      const createdCollection = await BookCollectionService.createBookCollection(data);

      // Rediriger vers la page de la collection nouvellement créée
      navigate(`/collections/user`);
    } catch (err: any) {
      console.error("Error creating smart collection:", err);
      setError(err.response?.data?.message || "Failed to create smart collection");
    } finally {
      setLoading(false);
    }
  };

  // Redirect to login if not authenticated
  if (!user) {
    return (
        <Layout>
          <div className="text-center py-8">
            <h1 className="text-2xl font-bold mb-4">Please login to create a smart collection</h1>
            <Link to="/login" className="text-indigo-600 hover:text-indigo-700">
              Go to login page
            </Link>
          </div>
        </Layout>
    );
  }

  return (
      <Layout>
        <div className="flex-1 bg-gray-100 p-8">
          <div className="max-w-2xl mx-auto">
            <div className="bg-white shadow-md rounded-lg overflow-hidden">
              {/* En-tête */}
              <div className="bg-gradient-to-r from-purple-600 to-indigo-600 p-6">
                <h1 className="text-3xl font-bold text-white">Create Smart Collection</h1>
                <p className="text-purple-100 mt-2">
                  Smart collections automatically include books based on your specified criteria
                </p>
              </div>

              {/* Formulaire */}
              <form onSubmit={handleSubmit} className="p-6">
                {error && (
                    <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 text-red-700">
                      <p>{error}</p>
                    </div>
                )}

                {/* Titre */}
                <div className="mb-4">
                  <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                    Collection Title *
                  </label>
                  <input
                      type="text"
                      id="title"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                      placeholder="Enter a title for your collection"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      required
                  />
                </div>

                {/* Description */}
                <div className="mb-4">
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                    Description
                  </label>
                  <textarea
                      id="description"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                      placeholder="Describe what this collection is about"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      rows={3}
                  />
                </div>

                {/* Visibilité */}
                <div className="mb-6">
                  <label className="flex items-center">
                    <input
                        type="checkbox"
                        className="h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
                        checked={isPublic}
                        onChange={(e) => setIsPublic(e.target.checked)}
                    />
                    <span className="ml-2 text-sm text-gray-700">Make this collection public</span>
                  </label>
                </div>

                <div className="border-t border-gray-200 pt-6 mb-6">
                  <h2 className="text-lg font-semibold text-gray-800 mb-4">Collection Criteria</h2>
                  <p className="text-sm text-gray-600 mb-4">
                    Books matching these criteria will be automatically added to this collection.
                  </p>

                  {/* Type de critère */}
                  <div className="mb-4">
                    <label htmlFor="criteria-type" className="block text-sm font-medium text-gray-700 mb-1">
                      Criteria Type *
                    </label>
                    <select
                        id="criteria-type"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                        value={criteriaType}
                        onChange={(e) => setCriteriaType(e.target.value)}
                    >
                      {CRITERIA_TYPES.map((type) => (
                          <option key={type.value} value={type.value}>
                            {type.label}
                          </option>
                      ))}
                    </select>
                  </div>

                  {/* Valeur du critère */}
                  <div className="mb-6">
                    <label htmlFor="criteria-value" className="block text-sm font-medium text-gray-700 mb-1">
                      Criteria Value *
                    </label>
                    <input
                        type="text"
                        id="criteria-value"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                        placeholder={`Enter ${CRITERIA_TYPES.find(t => t.value === criteriaType)?.label.toLowerCase()}`}
                        value={criteriaValue}
                        onChange={(e) => setCriteriaValue(e.target.value)}
                        required
                    />
                    <p className="mt-1 text-sm text-gray-500">
                      {criteriaType === "AUTHOR" && "Example: 'J.K. Rowling'"}
                      {criteriaType === "GENRE" && "Example: 'Fantasy', 'Science Fiction'"}
                      {criteriaType === "PUBLICATION_YEAR" && "Example: '2022' or '2010-2020'"}
                      {criteriaType === "TITLE_CONTAINS" && "Example: 'Harry Potter', 'Chronicles'"}
                    </p>
                  </div>
                </div>

                {/* Boutons d'action */}
                <div className="flex justify-end space-x-4">
                  <Link
                      to="/collections/user"
                      className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 focus:outline-none"
                  >
                    Cancel
                  </Link>
                  <button
                      type="submit"
                      className="px-4 py-2 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-md hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2"
                      disabled={loading}
                  >
                    {loading ? (
                        <span className="flex items-center">
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Creating...
                    </span>
                    ) : (
                        "Create Smart Collection"
                    )}
                  </button>
                </div>
              </form>
            </div>

            <div className="mt-6 text-center">
              <Link to="/collections/user" className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                Back to My Collections
              </Link>
            </div>
          </div>
        </div>
      </Layout>
  );
};

export default CreateSmartCollection;