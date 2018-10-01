package com.nerdzk.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import sun.management.GarbageCollectionNotifInfoCompositeData;
import sun.rmi.runtime.Log;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch; //ultilizada pra criar e colocar imagens ou texturas
    private Texture[] passaros;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture fundo;
    private Texture gameOver;
    private Random numeroRandomico;
    private BitmapFont fonte;
    private BitmapFont mensagem;
    private Circle passaroCirculo;
    private Rectangle retanguloCanoTopo;
    private Rectangle retanguloCanobaixo;



    //Atributos de Configuração
    private float larguraDispositivo;
    private float alturaDispositivo;
    private int canoBaixoAltura;
    private int canoAltoAltura;
    private int altura;
    private int espacoEntreCanos;
    private int estadoJogo = 0; // 0 -> jogo não iniciado / 1 -> jogo iniciado / 2 -> Game Over
    private int pontuacao = 0;

    private float variacao = 0;
    private float velocidadeQueda = 0;
    private float posicaoInicialVertical;
    private float posicaoMovimentoCanoHorizontal;
    private float deltaTime;

    private boolean marcouPonto = false;

    //Câmera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float VIRTUAL_HEIGHT = 1024;



    @Override
	public void create () {

	    batch = new SpriteBatch();
        numeroRandomico = new Random();
        passaroCirculo = new Circle();
        fonte = new BitmapFont();
        fonte.setColor(Color.YELLOW);
        fonte.getData().setScale(4);

        mensagem = new BitmapFont();
        mensagem.setColor(Color.YELLOW);
        mensagem.getData().setScale(3);

	    passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");

        fundo = new Texture("fundo.png");

        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");

        gameOver = new Texture("game_over.png");


        /*
            ******************************************************************
            Configurações câmera
            ******************************************************************
        */
        camera = new OrthographicCamera();
        camera.position.set( VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport( VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera );


        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        posicaoInicialVertical = alturaDispositivo / 2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 100;

        altura = numeroRandomico.nextInt(canoTopo.getHeight());
        canoAltoAltura =  Gdx.graphics.getHeight() - altura;
        canoBaixoAltura = altura * (-1);
    }

	@Override
	public void render () {

        camera.update();

        //Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        deltaTime = Gdx.graphics.getDeltaTime();
        variacao += deltaTime * 10;


        if (variacao > 2) variacao = 0;

        //Configurar dados de projeção da câmera
        batch.setProjectionMatrix( camera.combined );


        batch.begin();

        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);

        if ( estadoJogo == 0 ){ //Jogo não iniciado
            if (Gdx.input.justTouched()){ //apartir do primeiro toque na tele o jogo se inicia
                estadoJogo = 1;
            }
        }

        else {

            velocidadeQueda++;

            if (posicaoInicialVertical > 0 || velocidadeQueda < 0) {

                posicaoInicialVertical -= velocidadeQueda;
            }

            else {

                posicaoInicialVertical = 0;
            }

            if( estadoJogo == 1) {

                posicaoMovimentoCanoHorizontal -= deltaTime * 300;

                if (Gdx.input.justTouched()) { // retorna true se a tela for tocada

                    velocidadeQueda = -15;
                }

                //Verifica se o cano saiu inteiramente da tela
                if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {

                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                    altura = numeroRandomico.nextInt(canoTopo.getHeight());
                    canoAltoAltura = Gdx.graphics.getHeight() - altura;
                    canoBaixoAltura = altura * (-1);
                    marcouPonto = false;
                }

                batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, canoAltoAltura);
                batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, canoBaixoAltura);

                //marca a pontuação
                if( posicaoMovimentoCanoHorizontal < 120){
                    if( !marcouPonto){
                        pontuacao++;
                        marcouPonto = true;
                    }

                }

                //Teste de Colisão
                if ( Intersector.overlaps( passaroCirculo, retanguloCanobaixo ) || Intersector.overlaps( passaroCirculo, retanguloCanoTopo )
                        || posicaoInicialVertical <= 0){

                    estadoJogo = 2;
                }
            }
            else{//Tela de Game Over

                if( Gdx.input.justTouched()){

                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    posicaoInicialVertical = alturaDispositivo / 2;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                }
            }
        }

        batch.draw(passaros[(int) variacao], 120, posicaoInicialVertical);
        fonte.draw( batch, String.valueOf(pontuacao), larguraDispositivo/2 , alturaDispositivo - 50);

        if( estadoJogo == 2 ){

            batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, canoAltoAltura);
            batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, canoBaixoAltura);
            batch.draw( gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2, alturaDispositivo / 2);
            mensagem.draw( batch, "Tap to Restart", larguraDispositivo / 2 - 150, alturaDispositivo / 2 - gameOver.getHeight() / 2);
        }

        batch.end();

        passaroCirculo.set( 120 + passaros[ 0 ].getWidth() / 2, posicaoInicialVertical + passaros[ 0 ].getHeight() / 2, passaros[ 0 ].getHeight() / 2);
        retanguloCanobaixo = new Rectangle( posicaoMovimentoCanoHorizontal, canoBaixoAltura, canoBaixo.getWidth(), canoBaixo.getHeight());
        retanguloCanoTopo = new Rectangle( posicaoMovimentoCanoHorizontal, canoAltoAltura, canoTopo.getWidth(), canoTopo.getHeight());

        retanguloCanoTopo = new Rectangle( posicaoMovimentoCanoHorizontal, canoAltoAltura, canoTopo.getWidth(), canoTopo.getHeight() );
    }

    @Override
    public void resize( int width, int height){

        viewport.update( width, height );
    }
}
