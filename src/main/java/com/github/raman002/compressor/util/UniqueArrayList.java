package com.github.raman002.compressor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rewatiraman Singh Chandrol
 */
public class UniqueArrayList<T> extends ArrayList<T>
{
    private static final long serialVersionUID = 2544842829432559133L;

    public UniqueArrayList()
    {
        super();
    }

    public UniqueArrayList(int initialCapacity)
    {
        super(initialCapacity);
    }

    /*  Check if the given element is already present, If not then add it to the list.
     */
    @Override
    public boolean add(final T t)
    {
        return !contains(t) ? super.add(t) : false;
    }

    /* Check if the given element is already present at the specified index, If not then add it to the list.
     */
    @Override
    public void add(final int index, final T element)
    {
        if (!contains(get(index)))
        {
            super.add(index, element);
        }
    }

    /* Check if the given elements are already present, If not then add those elements to the list.
     */
    @Override
    public boolean addAll(final Collection<? extends T> c)
    {
        return !containsAll(c) ? super.addAll(c) : false;
    }

    /* Check if the given elements are already present at the specified index, If not then add those elements to the list.
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends T> c)
    {
        if (!containsAll(c))
        {
            super.addAll(index, c);
        }

        return false;
    }

    /**
     * @param objects
     * @return - UniqueArrayList instance containing the given elements.
     */
    @SafeVarargs
    public static <T> UniqueArrayList<T> asUniqueList(final T...objects)
    {
        final UniqueArrayList<T> uniqueList = new UniqueArrayList<>();

        for (T object : objects)
        {
            uniqueList.add(object);
        }

        return uniqueList;
    }

    public static <T> UniqueArrayList<T> asUniqueList(final List<T> list)
    {
        final UniqueArrayList<T> uniqueList = new UniqueArrayList<>();

        list.forEach(uniqueList::add);

        return uniqueList;
    }
}
