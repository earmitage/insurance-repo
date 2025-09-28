export interface InboundMessage {
    id?: number,
    payload?: string,
    errorCode?: number,
    sequenceNumber?: number,
    status?: string,
    isCleared?: boolean,
    processingDuration?: number,
    confirmed?: boolean,
    processed?: boolean,
    messageType: string,
    papssId: string,
    countryCode: string,
    participantBic: string,
    systemDate: any
}