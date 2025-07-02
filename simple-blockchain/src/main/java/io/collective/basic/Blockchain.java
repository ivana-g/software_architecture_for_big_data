package io.collective.basic;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {

    private final List<Block> chain = new ArrayList<>();

    public boolean isEmpty() {
        return chain.isEmpty();
    }

    public void add(Block block) {
        chain.add(block);
    }

    public int size() {
        return chain.size();
    }

    public boolean isValid() throws NoSuchAlgorithmException {
        if (chain.isEmpty()) {
            return true;
        }
        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            if (!isMined(block)) {
                return false;
            }
            if (!block.getHash().equals(block.calculatedHash())) {
                return false;
            }
            if (i > 0) {
                Block prev = chain.get(i - 1);
                if (!block.getPreviousHash().equals(prev.getHash())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Block mine(Block block) throws NoSuchAlgorithmException {
        Block mined = new Block(block.getPreviousHash(), block.getTimestamp(), block.getNonce());

        while (!isMined(mined)) {
            mined = new Block(mined.getPreviousHash(), mined.getTimestamp(), mined.getNonce() + 1);
        }
        return mined;
    }

    public static boolean isMined(Block minedBlock) {
        return minedBlock.getHash().startsWith("00");
    }
}