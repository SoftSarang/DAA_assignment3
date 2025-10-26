package com.aitu;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class GraphVisualizer {

    public static void main(String[] args) {
        try {
            String inputContent = new String(Files.readAllBytes(new File("data/input.json").toPath()));
            JSONObject input = new JSONObject(inputContent);
            JSONArray graphs = input.getJSONArray("graphs");

            String outputContent = new String(Files.readAllBytes(new File("data/output.json").toPath()));
            JSONObject output = new JSONObject(outputContent);
            JSONArray results = output.getJSONArray("results");

            File outputDir = new File("graphs/html");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            for (int i = 0; i < graphs.length(); i++) {
                JSONObject graph = graphs.getJSONObject(i);
                int graphId = graph.getInt("id");

                JSONArray primMstEdges = null;
                for (int j = 0; j < results.length(); j++) {
                    JSONObject result = results.getJSONObject(j);
                    if (result.getInt("graph_id") == graphId) {
                        primMstEdges = result.getJSONObject("prim").getJSONArray("mst_edges");
                        break;
                    }
                }

                String html = generateHTML(graph, primMstEdges);
                String filename = "graphs/html/graph_" + graphId + ".html";

                try (FileWriter writer = new FileWriter(filename)) {
                    writer.write(html);
                }
            }

            generateIndex(graphs, "graphs/html/index.html");
            System.out.println("Generated " + graphs.length() + " visualizations");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateHTML(JSONObject graph, JSONArray mstEdges) {
        int id = graph.getInt("id");
        JSONArray nodes = graph.getJSONArray("nodes");
        JSONArray edges = graph.getJSONArray("edges");

        Set<String> mstSet = new HashSet<>();
        if (mstEdges != null) {
            for (int i = 0; i < mstEdges.length(); i++) {
                JSONObject mstEdge = mstEdges.getJSONObject(i);
                String from = mstEdge.getString("from");
                String to = mstEdge.getString("to");
                mstSet.add(from + "-" + to);
                mstSet.add(to + "-" + from);
            }
        }

        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<title>Graph " + id + "</title>\n" +
                "<script src='https://d3js.org/d3.v7.min.js'></script>\n<style>\n" +
                "body{font-family:Arial;margin:0;padding:20px;background:#f5f5f5}\n" +
                ".container{max-width:1600px;margin:0 auto;background:white;padding:20px;border-radius:10px}\n" +
                "h1{color:#333;margin:0 0 10px 0}\n" +
                ".layout{display:grid;grid-template-columns:250px 1fr;gap:20px}\n" +
                ".sidebar{background:#f8f9fa;padding:15px;border-radius:8px;max-height:700px;overflow-y:auto}\n" +
                ".info{background:#e3f2fd;padding:15px;border-radius:5px;margin-bottom:15px;font-size:14px}\n" +
                ".controls{margin-bottom:15px}\n" +
                "button{background:#2196F3;color:white;border:none;padding:8px 15px;margin:3px;border-radius:5px;cursor:pointer;font-size:13px;width:100%}\n" +
                "button:hover{background:#1976D2}\n" +
                "button.reset{background:#FF5722}\n" +
                "button.mst{background:#4CAF50}\n" +
                ".vertex-list{margin-top:15px}\n" +
                ".vertex-item{background:white;padding:10px;margin:5px 0;border-radius:5px;cursor:pointer;border:2px solid transparent;font-size:13px}\n" +
                ".vertex-item:hover{border-color:#2196F3;background:#e3f2fd}\n" +
                ".vertex-item.selected{background:#4CAF50;color:white;border-color:#4CAF50}\n" +
                ".vertex-item .degree{float:right;color:#999;font-size:11px}\n" +
                ".vertex-item.selected .degree{color:white}\n" +
                ".search-box{width:100%;padding:8px;margin-bottom:10px;border:1px solid #ddd;border-radius:5px;box-sizing:border-box}\n" +
                "#graph{border:1px solid #ddd;background:white;border-radius:5px}\n" +
                ".links line{stroke:#999;stroke-opacity:0.6;stroke-width:1.5px}\n" +
                ".links line.hidden{display:none}\n" +
                ".links line.mst{stroke:#4CAF50;stroke-width:2.5px;stroke-opacity:0.9}\n" +
                ".links line.filtered{stroke:#FF5722;stroke-width:3px;stroke-opacity:1}\n" +
                ".nodes circle{stroke:#fff;stroke-width:2px;fill:#2196F3;cursor:pointer}\n" +
                ".nodes circle.hidden{display:none}\n" +
                ".nodes circle.inactive{fill:#999;opacity:0.5}\n" +
                ".nodes circle.highlighted{fill:#4CAF50;r:18;stroke-width:3px}\n" +
                ".nodes circle.neighbor{fill:#FF9800}\n" +
                ".nodes text{font-size:11px;font-weight:bold;fill:white;pointer-events:none}\n" +
                ".nodes text.hidden{display:none}\n" +
                ".weight-label{fill:#1565C0;font-size:11px;font-weight:bold;pointer-events:none}\n" +
                ".weight-label.hidden{display:none}\n" +
                ".stats{margin-top:15px;padding:10px;background:#fff3e0;border-radius:5px;font-size:12px}\n" +
                ".stats div{margin:5px 0}\n" +
                ".legend{margin-top:10px;padding:10px;background:#e8f5e9;border-radius:5px;font-size:12px}\n" +
                ".legend-item{margin:5px 0}\n" +
                ".legend-color{display:inline-block;width:15px;height:15px;border-radius:2px;margin-right:8px;vertical-align:middle}\n" +
                "</style>\n</head>\n<body>\n<div class='container'>\n<h1>Graph " + id + "</h1>\n" +
                "<div class='layout'>\n<div class='sidebar'>\n<div class='info'>\n" +
                "Vertices: " + nodes.length() + "<br>Edges: " + edges.length() + "<br>" +
                "Avg Degree: " + String.format("%.1f", (double)(edges.length() * 2) / nodes.length()) + "\n</div>\n" +
                "<div class='controls'>\n<button class='reset' onclick='reset()'>Show All</button>\n" +
                "<button class='mst' onclick='showMST()'>Highlight MST</button>\n" +
                "<button onclick='toggleWeights()'>Toggle Weights</button>\n</div>\n" +
                "<input type='text' class='search-box' id='search' placeholder='Search...' onkeyup='search()'>\n" +
                "<div class='vertex-list' id='list'></div>\n" +
                "<div class='stats' id='stats'>\n<div>Showing: <strong id='count'>" + nodes.length() + "</strong> vertices</div>\n" +
                "<div>Connected edges: <strong id='neighbors'>0</strong></div>\n</div>\n" +
                "<div class='legend'>\n" +
                "<div class='legend-item'><span class='legend-color' style='background:#999;'></span>Inactive</div>\n" +
                "<div class='legend-item'><span class='legend-color' style='background:#FF9800;'></span>Neighbor</div>\n" +
                "<div class='legend-item'><span class='legend-color' style='background:#4CAF50;'></span>Selected</div>\n" +
                "<div class='legend-item'><span class='legend-color' style='background:#FF5722;'></span>Selected edge</div>\n" +
                "<div class='legend-item'><span class='legend-color' style='background:#4CAF50;'></span>MST edge</div>\n" +
                "</div>\n</div>\n" +
                "<div><svg id='graph' width='1100' height='700'></svg></div>\n</div>\n" +
                "<script>\nconst data=" + nodesToJSON(nodes) + ";\nconst links=" + edgesToJSON(edges) + ";\n" +
                "const mstEdges=" + convertMSTtoJSON(mstSet) + ";\n" +
                "const adj={};\ndata.forEach(n=>adj[n.id]=[]);\n" +
                "links.forEach(l=>{adj[l.source].push(l.target);adj[l.target].push(l.source);});\n" +
                "const w=1100,h=700;\nconst svg=d3.select('#graph');\nconst g=svg.append('g');\n" +
                "const zoom=d3.zoom().scaleExtent([0.3,3]).on('zoom',e=>g.attr('transform',e.transform));\n" +
                "svg.call(zoom);\n" +
                "const sim=d3.forceSimulation(data).force('link',d3.forceLink(links).id(d=>d.id).distance(100))\n" +
                ".force('charge',d3.forceManyBody().strength(-200)).force('center',d3.forceCenter(w/2,h/2))\n" +
                ".force('collision',d3.forceCollide().radius(25));\n" +
                "const link=g.append('g').attr('class','links').selectAll('line').data(links).enter().append('line');\n" +
                "link.each(function(d){const isMST=mstEdges.has(d.source.id+'-'+d.target.id)||mstEdges.has(d.target.id+'-'+d.source.id);d3.select(this).classed('mst',isMST);});\n" +
                "const weights=g.append('g').selectAll('text').data(links).enter().append('text')\n" +
                ".attr('class','weight-label').attr('text-anchor','middle').attr('dy',4)\n" +
                ".text(d=>d.weight.toFixed(1));\n" +
                "const node=g.append('g').attr('class','nodes').selectAll('g').data(data).enter().append('g')\n" +
                ".call(d3.drag().on('start',(e,d)=>{if(!e.active)sim.alphaTarget(0.3).restart();d.fx=d.x;d.fy=d.y;})\n" +
                ".on('drag',(e,d)=>{d.fx=e.x;d.fy=e.y;}).on('end',(e,d)=>{if(!e.active)sim.alphaTarget(0);d.fx=null;d.fy=null;}));\n" +
                "node.append('circle').attr('r',14).on('click',(e,d)=>{e.stopPropagation();if(!showingMST)filter(d.id);});\n" +
                "node.append('text').attr('text-anchor','middle').attr('dy',4).text(d=>d.id);\n" +
                "sim.on('tick',()=>{\nlink.attr('x1',d=>d.source.x).attr('y1',d=>d.source.y).attr('x2',d=>d.target.x).attr('y2',d=>d.target.y);\n" +
                "weights.attr('x',d=>(d.source.x+d.target.x)/2).attr('y',d=>(d.source.y+d.target.y)/2);\n" +
                "node.attr('transform',d=>`translate(${d.x},${d.y})`);\n});\n" +
                "let sel=null,wv=true,showingMST=false;\n" +
                "function filter(v){\nsel=v;\nconst neighbors=new Set(adj[v]);\n" +
                "link.classed('hidden',d=>(d.source.id!==v&&d.target.id!==v)).classed('filtered',d=>(d.source.id===v||d.target.id===v)).classed('mst',false);\n" +
                "node.select('circle').classed('highlighted',d=>d.id===v).classed('neighbor',d=>neighbors.has(d.id)&&d.id!==v).classed('inactive',d=>!neighbors.has(d.id)&&d.id!==v);\n" +
                "node.select('text').classed('hidden',false);\n" +
                "if(wv){weights.classed('hidden',d=>(d.source.id!==v&&d.target.id!==v));}\n" +
                "document.querySelectorAll('.vertex-item').forEach(i=>i.classList.toggle('selected',i.dataset.v===v));\n" +
                "document.getElementById('neighbors').textContent=neighbors.size;\n}\n" +
                "function reset(){\nsel=null;showingMST=false;\n" +
                "link.classed('hidden',false).classed('mst',d=>mstEdges.has(d.source.id+'-'+d.target.id)||mstEdges.has(d.target.id+'-'+d.source.id)).classed('filtered',false);\n" +
                "node.select('circle').classed('highlighted',false).classed('neighbor',false).classed('inactive',false);\n" +
                "node.select('text').classed('hidden',false);\n" +
                "if(wv){weights.classed('hidden',false);}\n" +
                "document.querySelectorAll('.vertex-item').forEach(i=>i.classList.remove('selected'));\n" +
                "document.getElementById('neighbors').textContent=0;\n}\n" +
                "function showMST(){\nshowingMST=!showingMST;sel=null;\n" +
                "if(showingMST){\nnode.select('circle').classed('highlighted',false).classed('neighbor',false).classed('inactive',false);\n" +
                "node.select('text').classed('hidden',false);\n" +
                "link.classed('hidden',d=>!(mstEdges.has(d.source.id+'-'+d.target.id)||mstEdges.has(d.target.id+'-'+d.source.id))).classed('mst',true).classed('filtered',false);\n" +
                "if(wv){weights.classed('hidden',d=>!(mstEdges.has(d.source.id+'-'+d.target.id)||mstEdges.has(d.target.id+'-'+d.source.id)));}\n" +
                "document.querySelectorAll('.vertex-item').forEach(i=>i.classList.remove('selected'));\n}\n" +
                "else{reset();}\n}\n" +
                "function toggleWeights(){\nwv=!wv;\nif(!wv){weights.classed('hidden',true);}\n" +
                "else if(sel){\nweights.classed('hidden',d=>(d.source.id!==sel&&d.target.id!==sel));}\n" +
                "else if(showingMST){\nweights.classed('hidden',d=>!(mstEdges.has(d.source.id+'-'+d.target.id)||mstEdges.has(d.target.id+'-'+d.source.id)));}\n" +
                "else{weights.classed('hidden',false);}}\n" +
                "function search(){\nconst q=document.getElementById('search').value.toLowerCase();\n" +
                "document.querySelectorAll('.vertex-item').forEach(i=>i.style.display=i.textContent.toLowerCase().includes(q)?'block':'none');\n}\n" +
                "const list=document.getElementById('list');\n" +
                "data.sort((a,b)=>a.id.localeCompare(b.id)).forEach(n=>{\n" +
                "const deg=adj[n.id].length;\nconst item=document.createElement('div');\n" +
                "item.className='vertex-item';item.dataset.v=n.id;\n" +
                "item.innerHTML=`${n.id} <span class='degree'>${deg}</span>`;\n" +
                "item.onclick=()=>{if(!showingMST)filter(n.id);};\nlist.appendChild(item);\n});\n" +
                "</script>\n</div>\n</body>\n</html>";
    }

    private static String nodesToJSON(JSONArray nodes) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < nodes.length(); i++) {
            if (i > 0) s.append(",");
            s.append("{\"id\":\"").append(nodes.getString(i)).append("\"}");
        }
        return s.append("]").toString();
    }

    private static String edgesToJSON(JSONArray edges) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < edges.length(); i++) {
            JSONObject e = edges.getJSONObject(i);
            if (i > 0) s.append(",");
            s.append("{\"source\":\"").append(e.getString("from")).append("\",");
            s.append("\"target\":\"").append(e.getString("to")).append("\",");
            s.append("\"weight\":").append(e.getDouble("weight")).append("}");
        }
        return s.append("]").toString();
    }

    private static String convertMSTtoJSON(Set<String> mstSet) {
        StringBuilder s = new StringBuilder("new Set([");
        boolean first = true;
        for (String edge : mstSet) {
            if (!first) s.append(",");
            s.append("\"").append(edge).append("\"");
            first = false;
        }
        return s.append("])").toString();
    }

    private static void generateIndex(JSONArray graphs, String file) throws IOException {
        StringBuilder h = new StringBuilder("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<title>Graphs</title>\n<style>\n");
        h.append("body{font-family:Arial;margin:20px;background:#f5f5f5}\n");
        h.append("h1{text-align:center;color:#333}\n");
        h.append(".grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:20px;max-width:1400px;margin:20px auto}\n");
        h.append(".card{background:white;padding:20px;border-radius:10px;box-shadow:0 2px 5px rgba(0,0,0,0.1)}\n");
        h.append(".card:hover{transform:translateY(-5px);box-shadow:0 5px 15px rgba(0,0,0,0.2)}\n");
        h.append(".card h3{margin:0 0 10px 0;color:#2196F3}\n");
        h.append(".card p{margin:5px 0;color:#666;font-size:14px}\n");
        h.append(".card a{display:inline-block;margin-top:15px;padding:10px 20px;background:#2196F3;color:white;text-decoration:none;border-radius:5px}\n");
        h.append(".card a:hover{background:#1976D2}\n");
        h.append("</style>\n</head>\n<body>\n<h1>Graph Visualizations</h1>\n<div class='grid'>\n");

        for (int i = 0; i < graphs.length(); i++) {
            JSONObject g = graphs.getJSONObject(i);
            int id = g.getInt("id");
            int v = g.getJSONArray("nodes").length();
            int e = g.getJSONArray("edges").length();

            h.append("<div class='card'>\n<h3>Graph ").append(id).append("</h3>\n");
            h.append("<p>Vertices: ").append(v).append("</p>\n");
            h.append("<p>Edges: ").append(e).append("</p>\n");
            h.append("<a href='graph_").append(id).append(".html'>View</a>\n</div>\n");
        }

        h.append("</div>\n</body>\n</html>");
        try (FileWriter w = new FileWriter(file)) {
            w.write(h.toString());
        }
    }
}
