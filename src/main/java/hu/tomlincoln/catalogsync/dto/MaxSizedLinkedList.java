package hu.tomlincoln.catalogsync.dto;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class MaxSizedLinkedList<E> extends LinkedList<E> {

    private final int maxSize;

    public MaxSizedLinkedList(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(E value) {
        check();
        return super.add(value);
    }

    // We won't use following overridden methods in this project but for completeness / secure-ness of this custom list they have to be here ;-)

    @Override
    public void push(E e) {
        check();
        super.push(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c.stream().limit(check()).collect(Collectors.toCollection(LinkedList::new)));
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return super.addAll(index, c.stream().limit(check()).collect(Collectors.toCollection(LinkedList::new)));
    }

    @Override
    public void addLast(E e) {
        check();
        super.addLast(e);
    }

    @Override
    public void addFirst(E e) {
        check();
        super.addFirst(e);
    }

    private int check() {
        int free = this.maxSize - this.size();
        if (free == 0) {
            throw new IllegalArgumentException("No more space in the list!");
        }
        return free;
    }
}
