import { MinimalUser } from "./minimal-user";
import { TaskStatus } from "./task-status";

export interface Task {
    uuid: string;
    title: string;
    description: string;
    endDate: Date;
    status: TaskStatus;
    farmUuid: string;
    assignee: any;
    createdBy: any;
    targetDate: Date;
    completedDate: Date;
    class: string; 
    project:string;
}