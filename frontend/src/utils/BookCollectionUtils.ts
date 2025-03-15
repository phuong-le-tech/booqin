import {BookCollectionShow} from "../model/BookCollection.ts";

export const getBookCount = (collection: BookCollectionShow) => {
    if (!collection) return 0;
    if (!collection.books) return 0;
    return collection.books.length;
};