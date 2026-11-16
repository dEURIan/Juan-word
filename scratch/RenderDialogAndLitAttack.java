package scratch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.tile.StandardTileComponent;
import juanword.tile.TileComponent;
import juanword.ui.BattlePanel;

public class RenderDialogAndLitAttack {
    public static void main(String[] args) throws Exception {
        DataManager dm = DataManager.getInstance();
        JuanWord app = new JuanWord();
        Player cardo = null;
        for (Player p : dm.getAllPlayers()) {
            if ("Cardo".equalsIgnoreCase(p.getPlayerName())) {
                cardo = p;
                break;
            }
        }
        if (cardo == null) cardo = new Player(3, "Cardo");
        app.setActivePlayer(cardo);

        // 1. Render Character Selection Dialog Panel
        JPanel dialogContent = new JPanel(new BorderLayout(0, 0));
        dialogContent.setSize(480, 400);
        dialogContent.setBackground(new Color(11, 19, 43));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(new Color(15, 25, 45));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 155, 15)),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel titleLbl = new JLabel("👤 PILIA ANG PROFILE (Select Account)");
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLbl.setForeground(new Color(255, 200, 50));
        JLabel subLbl = new JLabel("Pilia ang imong account aron makapadayon sa biyahe sa Davao");
        subLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        subLbl.setForeground(new Color(77, 166, 197));
        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subLbl, BorderLayout.SOUTH);

        DefaultListModel<Player> model = new DefaultListModel<>();
        for (Player p : dm.getAllPlayers()) model.addElement(p);
        JList<Player> list = new JList<>(model);
        list.setBackground(new Color(15, 25, 45));
        list.setSelectedIndex(2); // select Cardo
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object val, int idx, boolean isSel, boolean focus) {
                JPanel card = new JPanel(new BorderLayout(8, 2));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(4, 8, 4, 8),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isSel ? new Color(255, 200, 50) : new Color(38, 52, 85), isSel ? 2 : 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                    )
                ));
                card.setBackground(isSel ? new Color(28, 48, 92) : new Color(18, 28, 52));

                if (val instanceof Player pl) {
                    JLabel nameLbl = new JLabel("👤 " + pl.getPlayerName());
                    nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                    nameLbl.setForeground(isSel ? new Color(255, 200, 50) : Color.WHITE);

                    String info = "Naabli: Mapa " + pl.getUnlockedMap() + "  |  Score: " + pl.getScore()
                        + (pl.hasActiveBattle() ? "  •  ⚔ Gubat Naabli" : "");
                    JLabel infoLbl = new JLabel(info);
                    infoLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                    infoLbl.setForeground(pl.hasActiveBattle() ? new Color(255, 190, 80) : new Color(77, 166, 197));

                    card.add(nameLbl, BorderLayout.NORTH);
                    card.add(infoLbl, BorderLayout.SOUTH);
                }
                return card;
            }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.setBackground(new Color(15, 25, 45));
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 50, 80)));
        JButton selBtn = new JButton("Pilia (Select)");
        selBtn.setBackground(new Color(245, 185, 30));
        selBtn.setForeground(new Color(15, 20, 40));
        JButton newBtn = new JButton("Bag-o (New)");
        newBtn.setBackground(new Color(25, 45, 80));
        newBtn.setForeground(new Color(255, 200, 50));
        JButton delBtn = new JButton("Papasa (Delete)");
        delBtn.setBackground(new Color(65, 20, 30));
        delBtn.setForeground(new Color(255, 140, 150));
        JButton closeBtn = new JButton("Isara (Close)");
        closeBtn.setBackground(new Color(30, 40, 65));
        closeBtn.setForeground(Color.WHITE);
        btnBar.add(selBtn);
        btnBar.add(newBtn);
        btnBar.add(delBtn);
        btnBar.add(closeBtn);

        JScrollPane scroll = new JScrollPane(list);
        scroll.getViewport().setBackground(new Color(15, 25, 45));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(35, 50, 85)));

        dialogContent.add(headerPanel, BorderLayout.NORTH);
        dialogContent.add(scroll, BorderLayout.CENTER);
        dialogContent.add(btnBar, BorderLayout.SOUTH);
        dialogContent.doLayout();

        BufferedImage dlgImg = new BufferedImage(480, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gDlg = dlgImg.createGraphics();
        dialogContent.paint(gDlg);
        gDlg.dispose();
        ImageIO.write(dlgImg, "PNG", new File("scratch/latest_character_select_themed.png"));
        System.out.println("Rendered scratch/latest_character_select_themed.png");

        // 2. Render BattlePanel with Lit ATAKE Button (BALAY selected)
        BattleSession s = new BattleSessionBuilder().withPlayer(cardo).onMap(1).atStage(1).build();
        // Force grid tiles to spell B-A-L-A-Y at [0][0], [0][1], [0][2], [0][3], [1][0]
        TileComponent[][] g = s.getGrid();
        g[0][0] = new StandardTileComponent('B');
        g[0][1] = new StandardTileComponent('A');
        g[0][2] = new StandardTileComponent('L');
        g[0][3] = new StandardTileComponent('A');
        g[1][0] = new StandardTileComponent('Y');

        BattlePanel bp = new BattlePanel(app);
        bp.setSize(960, 540);
        bp.begin(s);

        // Click tiles 0,0 -> 0,1 -> 0,2 -> 0,3 -> 1,0
        // Simulate clicking them
        java.lang.reflect.Method onTileClicked = BattlePanel.class.getDeclaredMethod("onTileClicked", int.class, int.class);
        onTileClicked.setAccessible(true);
        onTileClicked.invoke(bp, 0, 0);
        onTileClicked.invoke(bp, 0, 1);
        onTileClicked.invoke(bp, 0, 2);
        onTileClicked.invoke(bp, 0, 3);
        onTileClicked.invoke(bp, 1, 0);

        bp.doLayout();
        BufferedImage bpImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gBp = bpImg.createGraphics();
        bp.paint(gBp);
        gBp.dispose();
        ImageIO.write(bpImg, "PNG", new File("scratch/latest_battle_atake_lit.png"));
        System.out.println("Rendered scratch/latest_battle_atake_lit.png");
    }
}
