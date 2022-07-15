package service;

import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node<Task>> historyStorage = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    @Override
    public void add(Task task) {
        if (task != null) {
            if (historyStorage.containsKey(task.getId())) {
                Node<Task> findNode = historyStorage.get(task.getId());
                if (tail == findNode) {
                    return;
                }
                removeNode(findNode);
            }
            Node<Task> newNode = linkLast(task);
            historyStorage.put(task.getId(), newNode);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        ArrayList<Task> result = new ArrayList<>();
        Node<Task> currentNode = head;
        while (currentNode != null) {
            result.add(currentNode.getData());
            currentNode = currentNode.getNext();
        }
        return result;
    }

    @Override
    public void remove(int id) {
        try {
            Node<Task> node = historyStorage.get(id);
            final Node<Task> prev = node.getPrev();
            unlinkLeftNode(node);
            if (node.getNext() == null) {
                tail = node.getPrev();
            } else {
                node.getNext().setPrev(prev);
                node.setNext(null);
            }
            node.setData(null);
            historyStorage.remove(id);
        } catch (NullPointerException e) {
            System.out.println("Задача не удалена из истории. Проверьте корректность заполненных данных.");
        }
    }

    private void removeNode(Node<Task> node) {
        final Node<Task> prev = node.getPrev();
        unlinkLeftNode(node);
        node.getNext().setPrev(prev);
        node.setNext(null);
        node.setData(null);
    }

    private void unlinkLeftNode(Node<Task> node) {
        final Node<Task> next = node.getNext();
        final Node<Task> prev = node.getPrev();
        if (prev == null) {
            head = next;
        } else {
            prev.setNext(next);
            node.setPrev(null);
        }
    }

    private Node<Task> linkLast(Task task) {
        final Node<Task> newNode = new Node<>(tail, task, null);
        if (isNull(head)) {
            head = newNode;
        } else {
            tail.setNext(newNode);
        }
        tail = newNode;
        return newNode;
    }
}
