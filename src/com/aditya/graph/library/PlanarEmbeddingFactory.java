package com.aditya.graph.library;

public class PlanarEmbeddingFactory
{
    public static IPlanarEmbeddingMethods GetPlanarEmbeddingStrategy(PlanarEmbeddingStrategies type)
    {
        switch (type)
        {
            case DMP:
                return new DMPPlanarEmbeddingStrategy();
            default:
                return null;
        }
    }
}
