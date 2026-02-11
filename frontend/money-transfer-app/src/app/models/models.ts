export interface User {
    id: string;
    username: string;
    holderName: string;
    accountNumber: string;
}

export interface Transaction {
    id: string;
    date: Date;
    type: 'DEBIT' | 'CREDIT';
    amount: number;
    description: string;
    status: 'SUCCESS' | 'FAILED';
    otherParty: string;
}

export interface AccountInfo {
    user: User;
    balance: number;
    transactions: Transaction[];
}
