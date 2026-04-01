import { Task, TaskStatus } from "../types/Task";

interface TaskItemProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (id: number) => void;
  onStatusChange: (id: number, status: TaskStatus) => void;
}

const STATUS_STYLES: Record<TaskStatus, string> = {
  TODO: "bg-gray-100 text-gray-700",
  IN_PROGRESS: "bg-blue-100 text-blue-700",
  DONE: "bg-green-100 text-green-700",
};

const STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: "To Do",
  IN_PROGRESS: "In Progress",
  DONE: "Done",
};

export default function TaskItem({ task, onEdit, onDelete, onStatusChange }: TaskItemProps) {
  const handleDelete = () => {
    if (window.confirm(`Delete "${task.title}"?`)) {
      onDelete(task.id!);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-4 flex flex-col gap-2">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-lg break-words flex-1">{task.title}</h3>
        <span className={`text-xs font-medium px-2 py-1 rounded-full whitespace-nowrap ${STATUS_STYLES[task.status]}`}>
          {STATUS_LABELS[task.status]}
        </span>
      </div>

      {task.description && (
        <p className="text-gray-600 text-sm break-words">{task.description}</p>
      )}

      {task.dueDate && (
        <p className="text-gray-500 text-xs">Due: {task.dueDate}</p>
      )}

      <div className="flex items-center justify-between mt-2 pt-2 border-t border-gray-100">
        <select
          value={task.status}
          onChange={(e) => onStatusChange(task.id!, e.target.value as TaskStatus)}
          className="text-sm border border-gray-300 rounded px-2 py-1 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>

        <div className="flex gap-2">
          <button
            onClick={() => onEdit(task)}
            className="text-sm text-blue-600 hover:text-blue-800 transition"
          >
            Edit
          </button>
          <button
            onClick={handleDelete}
            className="text-sm text-red-600 hover:text-red-800 transition"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}
