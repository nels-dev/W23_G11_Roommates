package csis3175.w23.g11.rooftown.posts.data.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import csis3175.w23.g11.rooftown.common.AppDatabase;
import csis3175.w23.g11.rooftown.common.CallbackListener;
import csis3175.w23.g11.rooftown.posts.data.local.PostDao;
import csis3175.w23.g11.rooftown.posts.data.model.Post;
import csis3175.w23.g11.rooftown.posts.data.remote.PostService;

public class PostRepository {
    private final PostDao postDao;
    private final PostService postService;
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());

    public PostRepository() {
        postDao = AppDatabase.getInstance().postDao();
        postService = new PostService();
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    public ListenerRegistration loadAndListenToPosts() {
        AsyncTask.execute(() -> posts.postValue(postDao.getPosts()));
        return postService.listenToAllPosts(this::remoteCallBackWithData);
    }

    public void remoteCallBackWithData(List<Post> remoteDocPosts) {
        AsyncTask.execute(() -> {
            postDao.insertOrUpdate(remoteDocPosts);
            posts.postValue(postDao.getPosts());
        });
    }

    public void createPost(Post post, CallbackListener<Void> callback) {
        AsyncTask.execute(() -> {
            post.setPostId(UUID.randomUUID());
            postDao.insert(post);
            postService.savePost(post, callback);
        });
    }

    public void updatePost(Post post, CallbackListener<Void> callback) {
        AsyncTask.execute(() -> {
            postDao.update(post);
            postService.savePost(post, callback);
        });
    }
}
