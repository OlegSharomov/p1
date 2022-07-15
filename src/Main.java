import util.Managers;
import service.TaskManager;
import models.Epic;
import models.Subtask;
import models.Task;

import static models.Status.DONE;
import static models.Status.IN_PROGRESS;
import static service.InMemoryTaskManager.getNextId;

public final class Main {

    public static void main(String[] args) {
        System.out.println("Проверка работы InMemoryTaskManager");
        TaskManager manager = Managers.getDefault();
        Task washTheDishes = new Task(getNextId(), "Вымыть посуду", "Чтобы жена не ругалась опять");
        manager.createNewTask(washTheDishes);
        Task takeOutTheTrash = new Task(getNextId(), "Вынести мусор", "В мусорный бак");
        manager.createNewTask(takeOutTheTrash);
        Epic finishTheSecondSprint = new Epic(getNextId(), "Закончить третий спринт",
                "Не такой уж и легкий  как оказалось");
        manager.createNewEpic(finishTheSecondSprint);
        Subtask theory = new Subtask(getNextId(), "Пройти теорию", "Закрепить теорию на задачках",
                3);
        manager.createNewSubtask(theory);
        Subtask practice = new Subtask(getNextId(), "Сделать задание",
                "Так что бы ревью пройти с первого раза (мечты мечты...)", 3);
        manager.createNewSubtask(practice);
        Epic shopping = new Epic(getNextId(), "Сходить в магазин", "Побалуем себя");
        manager.createNewEpic(shopping);
        Subtask buyFish = new Subtask(getNextId(), "Купить красную рыбу", "Говорят полезна для мозга",
                6);
        manager.createNewSubtask(buyFish);

        System.out.println("    Создали новые объекты. Теперь они выглядят так:");
        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
        Task washTheDishes2 = new Task(1, "Вымыть посуду", "Чтобы жена не ругалась опять");
        washTheDishes2.setStatus(IN_PROGRESS);
        manager.updateTask(washTheDishes2);
        manager.removeTaskById(2);
        System.out.println("    Обновили задачу id №1 с измененным статусом IN_PROGRESS и удалили вторую задачу:");
        System.out.println(manager.getAllTasks());
        Subtask theory2 = new Subtask(4, "Пройти теорию", "Закрепить теорию на задачках",
                3);
        theory2.setStatus(DONE);
        manager.updateSubtask(theory2);
        System.out.println("    Обновили подзадачу id №4 с новым статусом DONE:");
        System.out.println(manager.getAllEpics());
        Subtask practice2 = new Subtask(5, "Сделать задание", "Так что бы ревью пройти с первого " +
                "раза (мечты мечты...)", 3);
        practice2.setStatus(DONE);
        manager.updateSubtask(practice2);
        System.out.println("    Обновили подзадачу id №5 с новым статусом DONE. Сейчас епик выглядит так:");
        System.out.println(manager.getEpicById(3));
        Epic finishTheSecondSprint2 = new Epic(3, "Закончить второй спринт",
                "Не такой уж и легкий  как оказалось задания делать не просто");
        manager.updateEpic(finishTheSecondSprint2);
        System.out.println("    Обновили эпик id №3 с измененным описанием:");
        System.out.println(manager.getAllEpics());
        manager.removeSubtaskById(5);
        System.out.println("    Удалили подзадачу id №5 \" Сделать задание \":");
        System.out.println(manager.getAllEpics());
        Subtask startThirdSprint = new Subtask(getNextId(), "Начать проходить третий спринт",
                "Еще куча не пройденной теории", 3);
        manager.createNewSubtask(startThirdSprint);
        System.out.println("    Добавили новую подзадачу в эпик id №3:");
        System.out.println(manager.getAllEpics());
        Subtask startThirdSprint2 = new Subtask(8, "Начать проходить третий спринт",
                "Еще куча не пройденной теории", 3);
        startThirdSprint2.setStatus(IN_PROGRESS);
        manager.updateSubtask(startThirdSprint2);
        System.out.println("    Обновили подзадачу id №8 с новым статусом IN_PROGRESS:");
        System.out.println(manager.getAllEpics());
        manager.removeEpicById(6);
        System.out.println("    Удалили эпик id №6:");
        System.out.println(manager.getAllEpics());
        System.out.println("    Посмотрим на подзадачу №8:");
        System.out.println(manager.getSubtaskById(8));
        System.out.println("    Посмотрим на все подзадачи епика №3:");
        System.out.println(manager.getSubtasksOfEpic(3));
        Subtask finishFourthSprint = new Subtask(getNextId(), "Пора заканчивать четвертый спринт",
                "Было довольно интересно", 3);
        finishFourthSprint.setStatus(IN_PROGRESS);
        manager.createNewSubtask(finishFourthSprint);
        System.out.println("    Добавили новую подзадачу в епик id №3 со статусом IN_PROGRESS. " +
                "Сейчас список подзадач выглядит так:");
        System.out.println(manager.getAllSubtasks());
        manager.getTaskById(1);     // Уже были запрошены епик id3 и затем - подзадача id8
        manager.getSubtaskById(4);
        manager.getSubtaskById(4);
        manager.getEpicById(3);
        manager.getSubtaskById(9);
        manager.getSubtaskById(9);
        System.out.println("    Сейчас будет напечатана история вызова последних запрошенных задач:");
        for (Task task : manager.getHistoryInMemory().getHistory()) {
            System.out.println(task);
        }
        manager.getHistoryInMemory().remove(4);
        manager.removeTaskById(1);
        System.out.println("    Удалили из хранилища задачу №1 и удалили из истории просмотров подзадачу №4. " +
                "Сейчас история выглядит так:");
        for (Task task : manager.getHistoryInMemory().getHistory()) {
            System.out.println(task);
        }
        System.out.println("    Пытаемся получить задачу с неправильным епиком: ");
        System.out.println(manager.getTaskById(3));
        manager.clearAllTasks();
        System.out.println("    Очистили хранилище задач. Сейчас они выглядят так:" + manager.getAllTasks());
        manager.clearAllSubtasks();
        System.out.println("    Очистили хранилище подзадач. Сейчас они выглядят так: " + manager.getAllSubtasks());
        System.out.println("    А список епиков выглядит так: " + manager.getAllEpics());
        manager.clearAllEpics();
        System.out.println("    Очистили хранилище епиков. Сейчас они выглядят так: " + manager.getAllEpics());
        System.out.println("    Сейчас будет напечатана история вызова последних запрошенных задач:");
        for (Task task : manager.getHistoryInMemory().getHistory()) {
            System.out.println(task);
        }
    }
}
