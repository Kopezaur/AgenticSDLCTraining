import { useState, useEffect, useCallback } from "react";
import { Task, TaskStatus } from "../types/Task";
import * as taskApi from "../services/taskApi";
import TaskItem from "./TaskItem";
import TaskForm from "./TaskForm";

type SortField = "status" | "dueDate" | "none";

export default function TaskList() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | undefined>(undefined);
  const [serverErrors, setServerErrors] = useState<Record<string, string>>({});
  const [sortBy, setSortBy] = useState<SortField>("none");

  const fetchTasks = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await taskApi.getTasks();
      setTasks(data);
    } catch {
      setError("Failed to load tasks. Is the backend running?");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  const handleCreate = async (task: Omit<Task, "id">) => {
    try {
      setServerErrors({});
      await taskApi.createTask(task);
      setShowForm(false);
      fetchTasks();
    } catch (err: unknown) {
      if (isAxiosError(err) && err.response?.status === 400) {
        setServerErrors(err.response.data);
      } else {
        setError("Failed to create task.");
      }
    }
  };

  const handleUpdate = async (task: Omit<Task, "id">) => {
    if (!editingTask?.id) return;
    try {
      setServerErrors({});
      await taskApi.updateTask(editingTask.id, task);
      setEditingTask(undefined);
      setShowForm(false);
      fetchTasks();
    } catch (err: unknown) {
      if (isAxiosError(err) && err.response?.status === 400) {
        setServerErrors(err.response.data);
      } else {
        setError("Failed to update task.");
      }
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await taskApi.deleteTask(id);
      fetchTasks();
    } catch {
      setError("Failed to delete task.");
    }
  };

  const handleStatusChange = async (id: number, status: TaskStatus) => {
    const task = tasks.find((t) => t.id === id);
    if (!task) return;
    try {
      await taskApi.updateTask(id, { ...task, status });
      fetchTasks();
    } catch {
      setError("Failed to update status.");
    }
  };

  const handleEdit = (task: Task) => {
    setEditingTask(task);
    setShowForm(true);
    setServerErrors({});
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingTask(undefined);
    setServerErrors({});
  };

  const sortedTasks = [...tasks].sort((a, b) => {
    if (sortBy === "status") {
      const order: Record<TaskStatus, number> = { TODO: 0, IN_PROGRESS: 1, DONE: 2 };
      return order[a.status] - order[b.status];
    }
    if (sortBy === "dueDate") {
      if (!a.dueDate && !b.dueDate) return 0;
      if (!a.dueDate) return 1;
      if (!b.dueDate) return -1;
      return a.dueDate.localeCompare(b.dueDate);
    }
    return 0;
  });

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Task Manager</h1>
        {!showForm && (
          <button
            onClick={() => {
              setEditingTask(undefined);
              setShowForm(true);
              setServerErrors({});
            }}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition"
          >
            + Add Task
          </button>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
          <button onClick={() => setError(null)} className="ml-2 font-bold">
            x
          </button>
        </div>
      )}

      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask ? handleUpdate : handleCreate}
          onCancel={handleCancel}
          serverErrors={serverErrors}
        />
      )}

      {!loading && tasks.length > 0 && (
        <div className="flex items-center gap-2 mb-4">
          <span className="text-sm text-gray-500">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as SortField)}
            className="text-sm border border-gray-300 rounded px-2 py-1"
          >
            <option value="none">Default</option>
            <option value="status">Status</option>
            <option value="dueDate">Due Date</option>
          </select>
        </div>
      )}

      {loading ? (
        <p className="text-gray-500 text-center py-8">Loading tasks...</p>
      ) : tasks.length === 0 ? (
        <p className="text-gray-500 text-center py-8">No tasks yet. Create one to get started!</p>
      ) : (
        <div className="grid gap-4">
          {sortedTasks.map((task) => (
            <TaskItem
              key={task.id}
              task={task}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onStatusChange={handleStatusChange}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function isAxiosError(err: unknown): err is { response?: { status: number; data: Record<string, string> } } {
  return typeof err === "object" && err !== null && "response" in err;
}
