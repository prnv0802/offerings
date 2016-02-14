package com.eis.b2bmb.camel.custom.util;

import org.apache.camel.Body;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Envista Tech on 5/29/2015.
 */
public class SplitFlatFileTransactions {

    /**
     * Process method which will split transactions that have lines with H for Header and D for detail into
     * separate transactions
     *
     * @param body - Exchange Body
     * @return List of String containing each transaction
     * @throws IOException - if there is problem reading and splitting the body
     */
    public List<String> splitTransactions(@Body InputStream body) throws IOException{

        List<String> transactions = new java.util.ArrayList<String>();

        BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(body));
        String line = null;

        boolean firstLine = true;
        StringBuilder transaction = new java.lang.StringBuilder();

        while((line=br.readLine())!=null){
            if(firstLine) {
                transaction.append(line);
                transaction.append("\n");
                firstLine = false;
                continue;
            }

            if(line.indexOf("D") == 0) {
                transaction.append(line);
                transaction.append("\n");
            }

            if(line.indexOf("H") == 0) {
                transactions.add(transaction.toString());
                transaction = new java.lang.StringBuilder();
                transaction.append(line);
                transaction.append("\n");
            }
        }

        transactions.add(transaction.toString());
        return transactions;
    }
}