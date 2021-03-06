/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vcscore.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A simple list wich holds only weak references to the original objects.
 * @author  Martin Entlicher, Tyler Freeman
 */
public class WeakList<T> extends AbstractList<T> {

    private ArrayList<WeakReference<T>> items;

    /** Creates new WeakList */
    public WeakList() {
        items = new ArrayList<>();
    }

    public WeakList(Collection<T> c) {
        items = new ArrayList<>();
        addAll(0, c);
    }

    public void add(int index, T element) {
        items.add(index, new WeakReference<>(element));
    }

    public Iterator<T> iterator() {
        return new WeakListIterator();
    }

    public int size() {
        removeReleased();
        return items.size();
    }

    public T get(int index) {
        return items.get(index).get();
    }

    private void removeReleased() {
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            WeakReference ref = (WeakReference) it.next();
            if (ref.get() == null) items.remove(ref);
        }
    }

    private class WeakListIterator implements Iterator<T> {

        private final Iterator<WeakReference<T>> it;

        public WeakListIterator() {
            removeReleased();
            it = items.iterator();
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public T next() {
            return it.next().get();
        }

        public void remove() {
            it.remove();
        }

    }

}