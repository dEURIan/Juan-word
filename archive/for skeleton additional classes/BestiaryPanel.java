import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Bestiary: list of creatures (read from data/bestiary.csv) with descriptions. */
public class BestiaryPanel extends JPanel {
    private final List<String[]> creatures = new ArrayList<>();   // {id, name, stage, description}

    public BestiaryPanel(JuanWordApp app) {
        setLayout(new BorderLayout(10, 10));
        setBackground(JuanWordApp.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(JuanWordApp.label("BESTIARY", 32, JuanWordApp.GOLD), BorderLayout.NORTH);

        loadCreatures();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String[] c : creatures) model.addElement(c[1] + "  (Stage " + c[2] + ")");
        JList<String> list = new JList<>(model);
        list.setFont(new Font("Serif", Font.PLAIN, 18));

        JTextArea info = new JTextArea("Select a creature to read about it.");
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(new Font("Serif", Font.PLAIN, 20));
        info.setMargin(new Insets(12, 12, 12, 12));

        list.addListSelectionListener(e -> {
            int i = list.getSelectedIndex();
            if (i >= 0) info.setText(creatures.get(i)[1].toUpperCase() + "\n\n" + creatures.get(i)[3]
                    + "\n\n[Sprite image placeholder]\n(Discovery tracking will use bestiary_progress.csv)");
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), new JScrollPane(info));
        split.setDividerLocation(300);
        add(split, BorderLayout.CENTER);

        JButton back = JuanWordApp.btn("Main Menu");
        back.addActionListener(e -> app.show(JuanWordApp.MENU));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadCreatures() {
        Path file = Paths.get("data", "bestiary.csv");
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String[] f = lines.get(i).split(",", 4);
                if (f.length == 4) creatures.add(f);
            }
        } catch (IOException e) {
            creatures.add(new String[]{"X", "bestiary.csv not found", "-", "Put bestiary.csv in the data folder."});
        }
    }
}
