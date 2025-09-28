import { User } from "./user";

export interface CalendarInfo {
    id?: string;
    start: number;
    end?: number;
    title: string;
    type: string;
    allDay: boolean;
}