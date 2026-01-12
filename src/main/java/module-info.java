module org.dam2.adp.ecorastro {
    // Módulos de JavaFX (ya los tendrás)
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // ==========================================
    //    AÑADE ESTAS LÍNEAS (IMPORTANTE)
    // ==========================================

    // 1. Permite usar JPA y Hibernate
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // 2. Permite conectarse a SQL y usar nombres JNDI
    requires java.naming;
    requires java.sql;

    // 3. Permite abrir tus paquetes a JavaFX y Hibernate
    opens org.dam2.adp.ecorastro to javafx.fxml;
    exports org.dam2.adp.ecorastro;

    // AÑADE ESTO: Abre tus modelos para que Hibernate pueda leer las entidades
    // Si tu paquete de modelos es diferente, ajusta la ruta
    opens org.dam2.adp.ecorastro.model to org.hibernate.orm.core;
}