import axios from "axios";
import { Task } from "../types/Task";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

export const getTasks = () => api.get<Task[]>("/tasks").then((res) => res.data);

export const getTask = (id: number) =>
  api.get<Task>(`/tasks/${id}`).then((res) => res.data);

export const createTask = (task: Omit<Task, "id">) =>
  api.post<Task>("/tasks", task).then((res) => res.data);

export const updateTask = (id: number, task: Omit<Task, "id">) =>
  api.put<Task>(`/tasks/${id}`, task).then((res) => res.data);

export const deleteTask = (id: number) => api.delete(`/tasks/${id}`);
