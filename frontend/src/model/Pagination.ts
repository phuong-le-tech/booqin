export interface PageInfo {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface Page<T> extends PageInfo {
    content: T[];
    page: PageInfo
}