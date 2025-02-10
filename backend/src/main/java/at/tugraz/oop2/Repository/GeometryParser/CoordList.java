package at.tugraz.oop2.Repository.GeometryParser;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;

class CoordList{
    private enum Position { START, END };
    private class MatchPosition {
        MatchPosition(Position position, boolean reverse) {
            this.position = position;
            this.reverse = reverse;
        }

        public Position position;
        public boolean reverse;
    }

    private LinkedList<LinkedList<Coordinate>> lists = new LinkedList<>();

    private boolean isClosed(LinkedList<Coordinate> coords) {
        return coords.getFirst().equals(coords.getLast());
    }

    public void add(Coordinate[] coords) {
        if (lists.isEmpty()) 
            lists.add(new LinkedList<Coordinate>());

        LinkedList<Coordinate> coordsList = new LinkedList<>();
        Collections.addAll(coordsList, coords);

        for (LinkedList<Coordinate> list : lists) {
            MatchPosition matchPosition = getMatchPosition(list, coordsList);
            if (matchPosition != null) {
                insert(list, coordsList, matchPosition);
                merge(list);
                return;
            }
        }

        lists.add(coordsList);
    }

    public Coordinate[] getClosedCircle() {
        for (LinkedList<Coordinate> list : lists) {
            if (isClosed(list)) {
                lists.remove(list);
                return list.toArray(new Coordinate[0]);
            }
        }
        return null;
    }
    
    private MatchPosition getMatchPosition(LinkedList<Coordinate> list, LinkedList<Coordinate> coords)
    {
        if (list.isEmpty())
            return new MatchPosition(Position.START, false);

        if (list.getFirst().equals(coords.getFirst()) && list != lists.getFirst())
            return new MatchPosition(Position.START, true);
        if (list.getFirst().equals(coords.getLast()) && list != lists.getFirst())
            return new MatchPosition(Position.START, false);
        if (list.getLast().equals(coords.getFirst()))
            return new MatchPosition(Position.END, false);
        if (list.getLast().equals(coords.getLast()))
            return new MatchPosition(Position.END, true);

        return null;
    }

    private void insert(LinkedList<Coordinate> list, LinkedList<Coordinate> coords, MatchPosition matchPosition)
    {
        Iterator<Coordinate> it = coords.iterator();
        if (matchPosition.position == Position.START && !matchPosition.reverse
                || matchPosition.position == Position.END && matchPosition.reverse)
            it = coords.descendingIterator();

        while (it.hasNext()) {
            if (matchPosition.position == Position.START)
                list.addFirst(it.next());
            else
                list.addLast(it.next());
        }
    }

    private void merge(LinkedList<Coordinate> l)
    {
        for (LinkedList<Coordinate> list : lists) {
            if (l == list)
                continue;

            MatchPosition matchPosition = getMatchPosition(list, l);
            if (matchPosition != null) {
                insert(list, l, matchPosition);
                lists.remove(l);
                return;
            }
        }
    }
}
