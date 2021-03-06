package com.pw.quizwhizz.model.game;

import com.pw.quizwhizz.model.exception.IllegalNumberOfQuestionsException;
import com.pw.quizwhizz.model.exception.IllegalTimeOfAnswerSubmissionException;
import com.pw.quizwhizz.model.exception.ScoreCannotBeRetrievedBeforeGameIsClosedException;
import org.assertj.core.api.Java6Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Klasa testująca logikę biznesową gry, wykorzystująca obiekty mockujące biblioteki Mockito.
 *
 * @author Karolina Prusaczyk
 * @see Game
 */
@RunWith(MockitoJUnitRunner.class)
public class GameTest {
    private Category category;
    private List<Question> questions;
    private GameStateMachine gameStateMachine;
    @Mock
    private Player player1;
    @Mock
    private Player player2;
    private List<Score> scores;
    private List<Answer> answersOfP1;
    private List<Answer> answersOfP2;

    /**
     * Test konstruktora wykorzystywanego w kodzie produkcyjnym.
     *
     * @throws IllegalNumberOfQuestionsException
     */
    @Test
    public void publicConstructorTest() throws IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();

        Game game = new Game(category, questions);

        Java6Assertions.assertThat(game.getCategory()).isEqualTo(category);
        Java6Assertions.assertThat(game.getQuestions()).isEqualTo(questions);
    }

    /**
     * Test weryfikujacy rzucenie wyjatku IllegalNumberOfQuestionsException w przypadku proby stworzenia gry o nieodpowiedniej liczbie pytan.
     *
     * @throws IllegalNumberOfQuestionsException
     */
    @Test
    public void givenWrongNumberOfQuestions_WhenCreatingGame_ThenExceptionShouldBeThrown() throws IllegalNumberOfQuestionsException {
        Category category = mock(Category.class);
        List<Question> questions = mock(List.class);
        when(questions.size()).thenReturn(0);

        assertThatThrownBy(() -> new Game(category, questions)).isInstanceOf(IllegalNumberOfQuestionsException.class);
    }

    /**
     * Test sprawdzajacy, ze gracz będacy juz na liscie graczy nie zostanie do niej dodany ponownie.
     *
     * @throws IllegalNumberOfQuestionsException
     */
    @Test
    public void givenListContainingPlayer1_WhenAddingPlayer1ToGameAgain_ThenTheyShouldNotBeAdded() throws IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndGameStateMachine();

        game.addPlayer(player1);
        game.addPlayer(player1);

        assertThat(game.getPlayers().size()).isEqualTo(1);
    }

    /**
     * Test weryfikujacy, ze proba sprawdzenia wynikow gry po jej zamknięciu zaskutkuje zwroceniem wynikow
     *
     * @throws IllegalNumberOfQuestionsException
     */
    @Test
    public void givenCategoryQuestionsAndClosedGameStatus_WhenCheckScoresIsCalled_ThenScoresAreNotNull() throws IllegalNumberOfQuestionsException, ScoreCannotBeRetrievedBeforeGameIsClosedException {
        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndGameStateMachine();
        givenGameStateIsClosed();

        scores = game.checkScores();

        Java6Assertions.assertThat(game.getCategory()).isEqualTo(category);
        Java6Assertions.assertThat(game.getQuestions()).isEqualTo(questions);
        verify(gameStateMachine, times(1)).determineCurrentState();
        Java6Assertions.assertThat(scores).isNotNull();
    }

    /**
     * Test weryfikujacy, ze proba sprawdzenia wynikow gry przed jej zakonczeniem spowoduje rzucenie
     * wyjatkiem ScoreCannotBeRetrievedBeforeGameIsClosedException.
     *
     * @throws ScoreCannotBeRetrievedBeforeGameIsClosedException
     */
    @Test
    public void givenGameStateIsClosed_WhenGetScoresIsCalled_ThenExceptionIsThrown() throws ScoreCannotBeRetrievedBeforeGameIsClosedException, IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndGameStateMachine();
        givenGameStateIsNotClosed();

        assertThatThrownBy(() -> game.checkScores()).isInstanceOf(ScoreCannotBeRetrievedBeforeGameIsClosedException.class);
    }

    /**
     * Test sprawdzajacy, ze proba wyslania odpowiedzi do oceny po przewidzianym na to czasie zaskutkuje rzucenie
     * wyjatkiem IllegalTimeOfAnswerSubmissionException.
     *
     * @throws IllegalNumberOfQuestionsException
     * @throws IllegalTimeOfAnswerSubmissionException
     */
    @Test
    public void givenGameIsNotInProgress_WhenEvaluateAnswersIsCalled_ThenExceptionShouldBeThrown() throws IllegalNumberOfQuestionsException {
        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndGameStateMachine();
        givenTwoPlayersAreAddedToTheGame(game);
        givenGameIsNotInProgress(true);

        assertThatThrownBy(() -> game.evaluateAnswers(player1, answersOfP1)).isInstanceOf(IllegalTimeOfAnswerSubmissionException.class);
    }

    /**
     * Test potwierdzajacy, ze wynik dodany do listy wynikow gry nie zostanie do niej dodany ponownie.
     *
     * @throws IllegalNumberOfQuestionsException
     */
    @Test
    public void givenPlayer1sAnswersWereEvaluated_WhenEvaluatingTheSameAnswers_ThenScoreShouldNotBeAddedToScoresAgain() throws IllegalNumberOfQuestionsException, IllegalTimeOfAnswerSubmissionException, ScoreCannotBeRetrievedBeforeGameIsClosedException {
        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndGameStateMachine();
        Player player1 = mock(Player.class);
        game.addPlayer(player1);

        givenGameIsNotInProgress(false);
        answersOfP1 = givenMockedListOfOneCorrectAnswer();

        game.evaluateAnswers(player1, answersOfP1);
        game.evaluateAnswers(player1, answersOfP1);

        givenGameStateIsClosed();
        assertThat(game.checkScores().size()).isEqualTo(1);

    }

    /**
     * Test potwierdzajacy, ze najwyzszy wynik wsrod graczy zostaje poprawnie oznaczony jako najwyzszy.
     *
     * @throws IllegalNumberOfQuestionsException
     * @throws IllegalTimeOfAnswerSubmissionException
     * @throws ScoreCannotBeRetrievedBeforeGameIsClosedException
     */

    @Test
    public void given2PlayersAndTheirEvaluatedAnswers_WhenGetScoresIsCalled_ThenProperScoreOfProperPlayerShouldBeMarkedAsHighest() throws IllegalNumberOfQuestionsException, IllegalTimeOfAnswerSubmissionException, ScoreCannotBeRetrievedBeforeGameIsClosedException {

        givenMockedCategoryQuestionsAndGameStateMachine();
        given10Questions();
        Game game = givenGameWithCategoryQuestionsAndGameStateMachine();
        givenTwoPlayersAreAddedToTheGame(game);
        givenStartIsCalled(game);
        givenGameIsNotInProgress(false);

        answersOfP1 = givenMockedListOfOneCorrectAnswer();
        answersOfP2 = givenMockedListOfNoCorrectAnswers();

        game.evaluateAnswers(player1, answersOfP1);
        game.evaluateAnswers(player2, answersOfP2);

        givenGameStateIsClosed();

        scores = game.checkScores();

        verify(gameStateMachine, atLeast(2)).determineCurrentState();
        verify(gameStateMachine, times(1)).start();
        assertThat(game.getPlayers()).contains(player1);
        assertThat(game.getPlayers()).contains(player2);
        assertThat(scores.size()).isEqualTo(2);
        assertThat(scores.get(0).isHighest()).isTrue();
        assertThat(scores.get(0).getPlayer()).isEqualTo(player1);
        assertThat(scores.get(1).isHighest()).isFalse();
        assertThat(scores.get(1).getPlayer()).isEqualTo(player2);
    }


    private void givenMockedCategoryQuestionsAndGameStateMachine() {
        category = mock(Category.class);
        questions = mock(List.class);
        gameStateMachine = mock(GameStateMachine.class);
    }

    private void given10Questions() {
        when(questions.size()).thenReturn(10);
    }

    private Game givenGameWithCategoryQuestionsAndGameStateMachine() throws IllegalNumberOfQuestionsException {
        return new Game(category, questions, gameStateMachine);
    }

    private void givenStartIsCalled(Game game) {
        game.start();
    }

    private void
    givenGameStateIsNotClosed() {
        when(gameStateMachine.gameIsClosed()).thenReturn(false);
    }

    private void givenGameStateIsClosed() {
        when(gameStateMachine.gameIsClosed()).thenReturn(true);
    }

    private void givenGameIsNotInProgress(boolean t) {
        when(gameStateMachine.gameIsNotInProgress()).thenReturn(t);
    }

    private void givenTwoPlayersAreAddedToTheGame(Game game) {
        game.addPlayer(player1);
        game.addPlayer(player2);
    }

    private List<Answer> givenMockedListOfOneCorrectAnswer() {
        Answer correctAnswer = mock(Answer.class);
        when(correctAnswer.getIsCorrect()).thenReturn(true);

        List<Answer> answersOfP1 = mock(List.class);
        when(answersOfP1.size()).thenReturn(1);
        when(answersOfP1.get(0)).thenReturn(correctAnswer);
        return answersOfP1;
    }

    private List<Answer> givenMockedListOfNoCorrectAnswers() {
        return mock(List.class);
    }

}
