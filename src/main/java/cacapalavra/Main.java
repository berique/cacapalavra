package cacapalavra;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

/**
 * Created by Configura��o on 21/08/2015.
 */
public class Main {

    private final int LENGTH_ESTADO = 7;

    private final int LENGTH_CIDADE = 6;

    private Connection conn;

    public static void main(String args[]) throws Exception {
        new Main().run();
    }

    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:file:target/db/flyway_sample";
        String user = "sa";
        String pwds = "";
        return DriverManager.
                getConnection(url, user, pwds);
    }

    public void run() throws Exception {
        conn = getConnection();

        String[][] cacaPalavras = montaMatrixCacaPalavra();
        List<String> completo = geraLista(cacaPalavras);
        Map<String, Set<String>> estadoECidade = geraEstadoECidade();

        String encontrou = procuraPalavra(completo, estadoECidade);
        if (encontrou != null) {
            System.out.println("ENCONTROU:");
            System.out.println(encontrou);
        } else {
            System.out.println("NÃO FOI ENCONTRADO");
        }
        conn.close();
    }

    private String procuraPalavra(List<String> completo, Map<String, Set<String>> estadoECidade) {
        String encontrou = null;
        for (String estado : estadoECidade.keySet()) {
            for (String cidade : estadoECidade.get(estado)) {
                for (String atual : completo) {
                    if (atual.equals(cidade)) {
                        encontrou = atual;
                        break;
                    }
                }
                if (encontrou != null) {
                    break;
                }
            }
            if (encontrou != null) {
                break;
            }
        }
        return encontrou;
    }

    private List<String> geraLista(String[][] cacaPalavras) {
        List<String> completo = new ArrayList<>();
        List<String> tmpList = new ArrayList<>();
        for (int x = 0; x < cacaPalavras.length; x++) {
            String tmp = "";
            for (int y = 0; y < cacaPalavras[x].length; y++) {
                tmp += cacaPalavras[x][y];
            }
            tmpList.add(tmp);
        }

        for (int x = 0; x < cacaPalavras.length; x++) {
            String tmp = "";
            for (int y = 0; y < cacaPalavras[x].length; y++) {
                tmp += cacaPalavras[y][x];
            }
            tmpList.add(tmp);
        }

        for (int x = 0; x < cacaPalavras.length; x++) {
            String tmp = "";
            tmp += cacaPalavras[x][x];
            tmpList.add(tmp);
        }

        for (int y = 0; y < (cacaPalavras.length / 2); y++) {
            String tmp = "";
            String tmp2 = "";
            int w = y;
            for (int x = 0; x < cacaPalavras.length; x++) {
                if (x > cacaPalavras.length) break;
                if (w + 1 > cacaPalavras.length) break;
                tmp2 += cacaPalavras[w][x];
                tmp += cacaPalavras[x][w++];
            }
            tmpList.add(tmp);
            tmpList.add(tmp2);
        }

        for (String tmp : tmpList) {
            for (int x = 0; x < tmp.length() - 1; x++) {
                if ((x + LENGTH_CIDADE) > tmp.length()) {
                    break;
                }
                completo.add(tmp.substring(x, x + LENGTH_CIDADE));
            }
        }
        return completo;
    }

    private Map<String, Set<String>> geraEstadoECidade() throws SQLException {
        Map<String, Set<String>> estadoECidade = new HashMap<>();
        String sqlEstado = "SELECT id,UPPER(nome) as nome FROM estado WHERE LENGTH(nome) = ?";
        PreparedStatement psEstado = null;
        ResultSet rsEstado = null;
        try {
            psEstado = conn.prepareStatement(sqlEstado);
            psEstado.setInt(1, LENGTH_ESTADO);
            rsEstado = psEstado.executeQuery();
            while (rsEstado.next()) {
                Integer idEstado = rsEstado.getInt("id");
                String estado = StringUtils.stripAccents(rsEstado.getString("nome"));
                geraCidade(estadoECidade, idEstado, estado);
            }
        } finally {
            if (psEstado != null) psEstado.close();
            if (rsEstado != null) rsEstado.close();
        }
        return estadoECidade;
    }

    private void geraCidade(Map<String, Set<String>> estadoECidade, Integer idEstado, String estado) throws SQLException {
        estadoECidade.put(estado, new HashSet<String>());
        PreparedStatement psCidade = null;
        ResultSet rsCidade = null;
        try {
            String sqlCidade = "SELECT UPPER(nome) as nome FROM cidade WHERE estado = ? AND LENGTH(nome) = ?";
            psCidade = conn.prepareStatement(sqlCidade);
            psCidade.setInt(1, idEstado);
            psCidade.setInt(2, LENGTH_CIDADE);
            rsCidade = psCidade.executeQuery();
            while (rsCidade.next()) {
                String cidade = StringUtils.stripAccents(rsCidade.getString("nome"));
                estadoECidade.get(estado).add(cidade);
            }
        } finally {
            if (psCidade != null) psCidade.close();
            if (rsCidade != null) rsCidade.close();
        }
    }

    private String[][] montaMatrixCacaPalavra() throws IOException {
        String[][] cacaPalavras = new String[10][10];
        int lineNumber = 0;
        InputStreamReader reader = new InputStreamReader(Main.class.getResourceAsStream("/cacapalavra.txt"));
        try (BufferedReader br = new BufferedReader(reader)) {
            for (String line; (line = br.readLine()) != null; ) {
                for (int x = 0; x < line.length(); x++) {
                    cacaPalavras[lineNumber][x] = line.substring(x, x + 1).toUpperCase();
                }
                lineNumber++;
            }
        }
        reader.close();
        return cacaPalavras;
    }
}
