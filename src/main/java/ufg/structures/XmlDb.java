package ufg.structures;

import java.util.ArrayList;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.utilities.UFGCRC;

public class XmlDb implements Serializable {

    public static class XmlDbNode implements Serializable {
        public int uid;
        public ArrayList<String> strings = new ArrayList<>();

        @SuppressWarnings("unchecked")
        @Override public XmlDbNode serialize(Serializer serializer, Serializable structure) {
            XmlDbNode node = (structure == null) ? new XmlDbNode() : (XmlDbNode) structure;
    
            node.uid = serializer.i32(node.uid);
            int count = serializer.u8(node.strings != null ? node.strings.size() : 0);
            if (!serializer.isWriting()) {
                node.strings = new ArrayList<>(count);
                for (int i = 0; i < count; ++i) {
                    serializer.u8(0);
                    node.strings.add(serializer.getInput().str( 0x80));
                }
            } else {
                for (int i = 0; i < node.strings.size(); ++i) {

                    if (node.strings.get(i).toLowerCase().endsWith(".bin"))
                        serializer.getOutput().u8(2);
                    else
                        serializer.getOutput().u8(1);


                    serializer.getOutput().str(node.strings.get(i), 0x80);
                }
            }

            return node;
        }
    
        @Override public int getAllocatedSize() { return 0xFFFF; }
    }

    private ArrayList<XmlDbNode> nodes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public XmlDb serialize(Serializer serializer, Serializable structure) {
        XmlDb db = (structure == null) ? new XmlDb() : (XmlDb) structure;

        db.nodes = serializer.arraylist(db.nodes, XmlDbNode.class);

        return db;
    }

    public XmlDbNode getNode(String path) {
        int uid = UFGCRC.qStringHash32(path.replaceAll("\\\\", "_").replaceAll("/", "_").replaceAll("\\.", "_").toUpperCase());
        for (XmlDbNode node : this.nodes) {
            if (node.uid == uid) return node;
        }
        return null;
    }

    public XmlDbNode addNode(String path) {
        XmlDbNode node = this.getNode(path);
        if (node != null) return node;
        int uid = UFGCRC.qStringHash32(path.replaceAll("\\\\", "_").replaceAll("/", "_").replaceAll("\\.", "_").toUpperCase());

        node = new XmlDbNode();
        node.uid = uid;

        this.nodes.add(node);

        return node;
    }

    // TODO: Actually generate proper allocation size
    @Override public int getAllocatedSize() { return 0xFFFF; }
}
