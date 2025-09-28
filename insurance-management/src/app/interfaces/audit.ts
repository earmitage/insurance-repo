export interface Audit {
    papssId: string;
    actionedBy: string;
    actionedDate: Date;
    paymentUuid: string;
    action: string;
    description: string;
}