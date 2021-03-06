package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraph implements Graph {

    private Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    private TinkerIndex index = new TinkerIndex();

    public Vertex addVertex(final Object id) {
        String idString;
        if (null != id) {
            idString = id.toString();
        } else {
            idString = this.getNextId();
        }

        Vertex vertex = this.vertices.get(idString);

        if (null != vertex) {
            return vertex;
        } else {
            vertex = new TinkerVertex(idString, this.index);
            this.vertices.put(vertex.getId().toString(), vertex);
            return vertex;
        }
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            return null;
        else {
            String idString = id.toString();
            return this.vertices.get(idString);
        }
    }

    public Iterable<Vertex> getVertices() {
        return vertices.values();
    }

    public Iterable<Edge> getEdges() {
        return new TinkerEdgeSequence(this.getVertices());
    }

    public void removeVertex(final Vertex vertex) {
        Set<Edge> toRemove = new HashSet<Edge>();
        for (Edge edge : vertex.getInEdges()) {
            toRemove.add(edge);
        }
        for (Edge edge : vertex.getOutEdges()) {
            toRemove.add(edge);
        }
        for (Edge edge : toRemove) {
            this.removeEdge(edge);
        }

        for (String key : vertex.getPropertyKeys()) {
            this.index.remove(key, vertex.getProperty(key), vertex);
        }
        this.vertices.remove(vertex.getId().toString());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        String idString;
        if (null != id) {
            idString = id.toString();
        } else {
            idString = this.getNextId();
        }

        TinkerVertex out = (TinkerVertex) outVertex;
        TinkerVertex in = (TinkerVertex) inVertex;
        TinkerEdge edge = new TinkerEdge(idString, outVertex, inVertex, label, this.index);
        out.outEdges.add(edge);
        in.inEdges.add(edge);
        //this.edges.save(edge.getId(), edge);
        return edge;
    }

    public void removeEdge(final Edge edge) {
        TinkerVertex outVertex = (TinkerVertex) edge.getOutVertex();
        TinkerVertex inVertex = (TinkerVertex) edge.getInVertex();
        if (null != outVertex && null != outVertex.outEdges)
            outVertex.outEdges.remove(edge);
        if (null != inVertex && null != inVertex.inEdges)
            inVertex.inEdges.remove(edge);
        //this.edges.delete(edge.getId());
    }

    public Index getIndex() {
        return this.index;
    }

    public String toString() {
        return "tinkergraph[vertices:" + this.vertices.size() + "]";
    }

    public void clear() {
        this.vertices.clear();
    }

    public void shutdown() {

    }

    private String getNextId() {
        String idString;
        while (true) {
            idString = this.currentId.toString();
            this.currentId++;
            if (null == this.vertices.get(idString) || this.currentId == Long.MAX_VALUE)
                break;
        }
        return idString;
    }

    private class TinkerEdgeSequence implements Iterator<Edge>, Iterable<Edge> {

        private Iterator<Vertex> vertices;
        private Iterator<Edge> currentEdges;
        private boolean complete = false;

        public TinkerEdgeSequence(final Iterator<Vertex> vertices) {
            this.vertices = vertices;
            this.complete = this.goToNextEdge();
        }

        public TinkerEdgeSequence(final Iterable<Vertex> vertices) {
            this(vertices.iterator());
        }

        public Edge next() {
            Edge edge = currentEdges.next();
            this.complete = this.goToNextEdge();
            return edge;
        }

        public boolean hasNext() {
            return !complete;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private boolean goToNextEdge() {
            if (this.currentEdges == null || !this.currentEdges.hasNext()) {
                if (vertices.hasNext()) {
                    this.currentEdges = vertices.next().getOutEdges().iterator();
                } else {
                    return true;
                }
            }

            if (this.currentEdges.hasNext()) {
                return false;
            } else {
                return this.goToNextEdge();
            }

        }

        public Iterator<Edge> iterator() {
            return this;
        }
    }
}
