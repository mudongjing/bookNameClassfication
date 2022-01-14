package com.example.demo.data.neo4j;


import com.example.demo.pojo.neo4j.Words;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BookNeo4jService {
    @Resource
    private BookRepository bookRepository;


    // 创建连接
    public boolean createRelation(String start,String end){
        if (start!= null && end != null){
            return bookRepository.createRelation(start,end);
        }
        return false;
    }
    // 获取节点的邻居
    public Collection<Words> getNeighborById(String word){
        if (word !=null){
            return  bookRepository.findNeighbour(word);
        }else return new HashSet<Words>();
    }
    public Set<String> getNeighborStringById(String id){
        Set<String> result = new HashSet<>();
        Collection<Words> neighborById = getNeighborById(id);
        neighborById.forEach(nei->{ result.add(nei.getWord()); });
        return result;
    }

    // 缩小节点间距离
    public boolean reduceDistance(String start,String end,int step){
        if (start!=null && end!=null){
            Optional<Words> startId = bookRepository.findById(start);
            Optional<Words> endId = bookRepository.findById(end);
            if (startId.isPresent() && endId.isPresent() && bookRepository.findRelationById(start, end) != null){
                bookRepository.setDistanceById(start, end, step);
                return true;
            }
        }
        return false;
    }

    @Data
    @AllArgsConstructor
    private static class PairOfStringAndDouble implements Comparable{
        private String string;
        private Double doub;

        @Override
        public int compareTo(Object o) {
            PairOfStringAndDouble obj = (PairOfStringAndDouble) o;
            if (this.doub < obj.getDoub()) return 1; //逆序
            else return -1;
        }
    }

    public Map<Integer,Set<String>> groupMapDivide(String leadRole){
        Set<Set<String>> sets = groupDivide(leadRole);
        Map<Integer,Set<String>> result = new HashMap<>();
        AtomicInteger t =new AtomicInteger(0);

        sets.forEach(v ->{
            result.put(t.getAndAdd(1),v);
        });

        return result;
    }

    /* 对leadRole节点的邻居做划分
    划分方式，从一个节点开始，遍历自己的邻居，并记录对应的影响权重，
    从邻居中选择最大的一个，再检查该邻居，判断本节点是否是该邻居的最大邻居，如果是，则划分为一个群组，群组有的邻居，
    找出最大的一个，如果这个节点的最大邻居属于该群组，则划分到该群组，直到对方不符合要求，
    */
    public Set<Set<String>> groupDivide(String leadRole){
        Set<String> neighborOfLeadRole = getNeighborStringById(leadRole);
        Map<String,TreeSet<PairOfStringAndDouble>> nodeWithNode = new HashMap<>(); // 已计算邻居影响权重 的节点的邻居信息
        Set<Set<String>> group = new HashSet<>(); // 存储完成的群组划分

        while(neighborOfLeadRole.size()>0){ // 随机从一个未分类的节点出发
            Set<String> groupNew = new HashSet<>();
            String nei = neighborOfLeadRole.iterator().next();
            // 已获得一个节点所有邻居的影响权重，从中取得最大值即可，随后移除该值
            // 如果对应的邻居已属于自己的群组，则查看下一个邻居，依次类推
            groupNew.add(nei);
            neighborOfLeadRole.remove(nei);
            TreeSet<PairOfStringAndDouble> biasFromNode = getBiasFromNode(nei);
            nodeWithNode.put(nei,biasFromNode);
            AtomicReference<Integer> size = new AtomicReference<>(groupNew.size());
            AtomicReference<Integer> flag = new AtomicReference<>(size.get());

            // 上面是未分类的节点组建为一个新的群组
            // 下面是一个正在建立 的群组不断扫描内部成员的邻居，以获取新的成员

            while(flag.get()>=0){// 对当前的分组中的节点逐一检查邻居，每次对一个节点刨根问底，如果未发现新节点，flag-1
                // 当群组加入 新节点，更新size，和flag,
                AtomicReference<PairOfStringAndDouble> newNode = new AtomicReference<>(new PairOfStringAndDouble(null,(double)0));
                groupNew.forEach(oldNode->{ // 遍历群组成员
                    TreeSet<PairOfStringAndDouble> otherNode = nodeWithNode.get(oldNode);
                    while(true){
                        PairOfStringAndDouble first = otherNode.first();
                        while(groupNew.contains(first.getString())){
                            otherNode.pollFirst();
                            if (otherNode.size()>0) first = otherNode.first();
                            else { first =null; break; }
                        }
                        boolean stop = false;
                        if (first!=null && neighborOfLeadRole.contains(first.getString())){// 可能的新成员
                            // 如果对方的优先节点不在我们这里，仍然需要终止
                            TreeSet<PairOfStringAndDouble> biasFromFirst ;
                            if (nodeWithNode.containsKey(first.getString())) biasFromFirst = nodeWithNode.get(first.getString());
                            else biasFromFirst = getBiasFromNode(first.getString());
                            PairOfStringAndDouble newFirst = biasFromFirst.first();
                            if (groupNew.contains(newFirst.getString())){
                                // 双方都符合要求，加入新成员 first
                                groupNew.add(first.getString());
                                // 更新
                                otherNode.pollFirst();biasFromFirst.pollFirst();
                                nodeWithNode.put(oldNode,otherNode);nodeWithNode.put(first.getString(),biasFromFirst);

                                size.set(groupNew.size()); flag.set(size.get());
                            }else stop =true;
                        }else stop =true;

                        if (stop){// 从该邻居开始，优先的群组不是我们，放弃此后的候选节点
                            flag.updateAndGet(v ->v-1);
                            nodeWithNode.put(oldNode,otherNode);
                            break;
                        }
                    }
                });
                group.add(groupNew);
            }
        }
        return group;
    }
    public Map<String,Double> getBiasMapFromNode(String leadRole){
        Set<PairOfStringAndDouble> neiSet = biasFromNode(leadRole);
        Map<String,Double> result = new HashMap<>();
        neiSet.forEach(v ->{
            result.put(v.getString(),v.getDoub());
        });
        return result;
    }

    public TreeSet<PairOfStringAndDouble> getBiasFromNode(String leadRole){
        Set<PairOfStringAndDouble> neiSet = biasFromNode(leadRole);
        return new TreeSet<>(neiSet);
    }
    private Set<PairOfStringAndDouble> biasFromNode(String leadRole){
        Collection<Words> neighborOfLeadRole = getNeighborById(leadRole);
        AtomicReference<Double> sum = new AtomicReference<>((double) 0);
        Set<PairOfStringAndDouble> neiSet = new HashSet<>();
        biasNeighborFromNode(neighborOfLeadRole,sum,neiSet,leadRole);

        return neiSet;
    }
    private void biasNeighborFromNode(Collection<Words> neighborOfLeadRole,AtomicReference<Double> sum,
                                      Set<PairOfStringAndDouble> neiSet,String leadRole){
        neighborOfLeadRole.forEach(nei ->{
            AtomicReference<Double> sum_nei = new AtomicReference<>((double)0);
            /*
             * 计算nei和所有其他nei的各种直接或间接连接 的路径 对应的影响权重
             */
            double nei_lead = 1/(double)getDistanceByIdAndString(nei.getWord(),leadRole);
            sum_nei.updateAndGet(v -> v + nei_lead); // 最初连接
            neighborOfLeadRole.forEach(other->{
                if (!nei.getWord().equals(other.getWord())){
                    double other_lead = 1/(double)getDistanceByIdAndString(other.getWord(),leadRole);
                    if (bookRepository.existsRelation(nei.getWord(),other.getWord())){
                        double nei_other = 1/(double) getDistanceByIdAndString(nei.getWord(),other.getWord());
                        sum_nei.updateAndGet(v-> v + nei_other * other_lead); // 邻居间的直接连接
                    }
                    sum_nei.updateAndGet(v-> v + other_lead * calculateMidRelation(nei.getWord(), other.getWord())); // 邻居间的间接连接
                }
            });
            sum.updateAndGet(v -> v + sum_nei.get());
            neiSet.add(new PairOfStringAndDouble(nei.getWord(),sum_nei.get()));
        });
    }
    // 计算节点之间的相对影响权重
    // 在 leadRole角度下，costar占据的影响权重
    // 计算leadRole 所有邻居的影响权重，取costar的比值
    // 我们现在不在局限于给定 的costar，而是所有可能的节点 ，包含costar,以及给定的一个节点集合 ，与leadRole自己的邻居集合
//    public double getBiasFromNode(String leadRole,String costar){
//        Collection<Words> neighborOfLead = getNeighborById(leadRole);
//        /*
//         * 计算leadrole所有邻居的影响权重，
//         * 由于是以leadRole 为中心点，所有 的邻居在计算时，都需要记录一个与leadRole的邻居交集，差集中的节点 则需要考虑是否可以通过其他的第二个节点建立连接，
//         * 也就是对于中心节点的一个邻居的影响权重计算，需要大致两个步骤：
//         * 1、 邻居与中心节点做邻居交集，邻居和交集的节点做计算,另外也许需要计算二者的邻居交集，计算所有路径产生的影响权重之和
//         * 2、 中心节点的邻居集合与上述交集做差集，差集中的节点再一一与邻居做邻居集合的交集，差集中的节点均代表一条路径，计算所有的路径权重附加到该节点上
//         */
//        AtomicReference<Double> sum = new AtomicReference<>((double) 0);
//        AtomicReference<Double> sum_costar = new AtomicReference<>((double) 0);
//        neighborOfLead.forEach(nei ->{
//            AtomicReference<Double> sum_nei = new AtomicReference<>((double)0);
//            /*
//             * 计算nei和所有其他nei的各种直接或间接连接 的路径 对应的影响权重
//             */
//            double nei_lead = 1/(double)getDistanceByIdAndString(nei.getWord(),leadRole);
//            sum_nei.updateAndGet(v -> v + nei_lead); // 最初连接
//            neighborOfLead.forEach(other->{
//                if (!nei.getWord().equals(other.getWord())){
//                    double other_lead = 1/(double)getDistanceByIdAndString(other.getWord(),leadRole);
//                    if (bookRepository.existsRelation(nei.getWord(),other.getWord())){
//                        double nei_other = 1/(double) getDistanceByIdAndString(nei.getWord(),other.getWord());
//                        sum_nei.updateAndGet(v-> v + nei_other * other_lead); // 邻居间的直接连接
//                    }
//                    sum_nei.updateAndGet(v-> v + other_lead * calculateMidRelation(nei.getWord(), other.getWord())); // 邻居间的间接连接
//                }
//            });
//            sum.updateAndGet(v -> v + sum_nei.get());
//            if (nei.getWord().equals(costar)) sum_costar.updateAndGet(v-> v+sum_nei.get());
//
//        });
//        return sum_costar.get()/sum.get();
//    }

    // 计算两个节点之间通过中间节点建立连接的所有路径产生的影响权重
    public double calculateMidRelation(String start,String end){
        Collection<Words> interSection = bookRepository.getInterSection(start, end);
        AtomicReference<Double> mid_sum = new AtomicReference<>((double)0);
        interSection.forEach(nei->{
            double start_nei = getDistanceByIdAndString(start,nei.getWord());
            double end_nei = getDistanceByIdAndString(nei.getWord(),end);
            mid_sum.updateAndGet(v -> v + 1 / (start_nei * end_nei));
        });
        return mid_sum.get();
    }

    public int getDistanceByIdAndString(String id,String neighbour){
        return bookRepository.findRelationById(id,neighbour).getDistance();
    }

    // 交集
    public Collection<Words> getIntersectionFromWords(Words lead,Words costar){
        return bookRepository.getInterSection(lead.getWord(),costar.getWord());
    }
    //  差集
    public Collection<Words> getDifference(Words lead,Words costar){
        Collection<Words> leadNei = bookRepository.findNeighbour(lead.getWord());
        Collection<Words> costarNei = bookRepository.findNeighbour(costar.getWord());
        leadNei.removeAll(costarNei);
        return leadNei;
    }

    public boolean existKeyword(String keyword){
        Optional<Words> byId = bookRepository.findById(keyword);
        return byId.isPresent();
    }
    public Words getWordsById(String words){
        Optional<Words> byId = bookRepository.findById(words);
        return byId.orElse(null);
    }
}
