package types;

import java.util.Objects;

/**
 * Diese Klasse enthält Methoden, welche mit den Wegepunkten einer Liste arbeitet.
 *
 * @author janik
 */
public class Position {

    private final int x;
    private final int y;

    /**
     * Setter-Methode für x und y
     *
     * @param x erste Wegpunkt Koordinate
     * @param y zweite Wegpunkt Koordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Getter-Methode für x
     *
     * @return x-Koordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Getter-Methode dür y
     *
     * @return y-Koordinate
     */
    public int getY() {
        return y;
    }


    /**
     * Berechnet die euklidische Entfernung zwischen zwei Positionen.
     *
     * @param other zweite types.Position
     * @return Entfernung
     */
    public double distance(Position other) {
        assert (other != null);
        return Math.sqrt(Math.pow((other.x - this.x), 2) + Math.pow((other.y - this.y), 2));
    }

    public Position normalize(Position other) {
        assert (other != null);
        double dist = distance(other);
        if (dist != 0) {
            return new Position((int) (x / dist), (int) (y / dist));
        }
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.getX() && y == position.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {

        return "(" + this.x + "/" + this.y + ")";
    }
}
