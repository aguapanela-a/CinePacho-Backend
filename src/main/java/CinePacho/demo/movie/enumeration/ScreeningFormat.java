package CinePacho.demo.movie.enumeration;

public enum ScreeningFormat {
    FORMAT_2D("2D"),
    FORMAT_3D("3D"),
    FORMAT_IMAX("IMAX");

    private final String displayName;

    ScreeningFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene el enum a partir del nombre mostrado (ej: "2D" -> FORMAT_2D)
     */
    public static ScreeningFormat fromDisplayName(String displayName) {
        for (ScreeningFormat format : ScreeningFormat.values()) {
            if (format.displayName.equalsIgnoreCase(displayName)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Formato desconocido: " + displayName);
    }
}
