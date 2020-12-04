package com.jd.bdp.datadev.domain;


public class DoubleLinkedList<T> {


    private Integer size = 0;

    private Node<T> head;
    private Node<T> tail;

    public DoubleLinkedList() {

    }

    /**
     * 1.头部添加Node
     * <p>
     * <p>
     * first -> seconed
     * <-
     * <p>
     * head     tail
     *
     * @param node
     * @return
     */
    private Node<T> addHeadNode(Node<T> node) {
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head.pre = node;
            head = node;
        }
        size++;
        return node;
    }
    private Node<T> addLastNode(Node<T> node) {
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.pre = tail;
            tail = node;
        }
        size++;
        return node;
    }

    private static class Node<T> {
        T t;
        Node<T> pre;
        Node<T> next;

        public Node(T t) {
            this.t = t;
        }

    }

    public static void main(String[] args) {
        DoubleLinkedList<String> linkedList = new DoubleLinkedList<>();

        linkedList.addHeadNode(new Node<String>("sss"));
        linkedList.addHeadNode(new Node<String>("sss1"));
        linkedList.addLastNode(new Node<String>("sss2"));


        //findCurrentSelectedIndex =



        System.out.println(linkedList.size);
    }
}
