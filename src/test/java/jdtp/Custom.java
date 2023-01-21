package jdtp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

class Custom implements Serializable {
    public int a = 0;
    public String b = "";
    public ArrayList<String> c = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        Custom other = (Custom) obj;

        return this.a == other.a && this.b.equals(other.b) && this.c.equals(other.c);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(a);
        out.writeUTF(b);
        out.writeInt(c.size());

        for (String cVal : c) {
            out.writeUTF(cVal);
        }
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        a = in.readInt();
        b = in.readUTF();
        c = new ArrayList<>();
        int cSize = in.readInt();

        for (int i = 0; i < cSize; i++) {
            c.add(in.readUTF());
        }
    }
}
